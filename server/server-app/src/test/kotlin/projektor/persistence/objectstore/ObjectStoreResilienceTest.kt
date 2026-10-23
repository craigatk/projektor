package projektor.persistence.objectstore

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import projektor.coverage.CoverageRepository
import projektor.incomingresults.model.GitMetadata
import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.incomingresults.model.ResultsMetadata
import projektor.incomingresults.randomPublicId
import projektor.metadata.TestRunMetadataRepository
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.objectstore.ObjectStoreTelemetry
import projektor.objectstore.WriteCondition
import projektor.objectstore.json.JsonObjectStore
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats
import projektor.performance.PerformanceResultsRepository
import projektor.persistence.PersistenceInstrumentation
import projektor.persistence.PersistenceType
import projektor.repository.testrun.RepositoryTestRunRepository
import projektor.server.api.PublicId
import projektor.testrun.TestRunRepository
import projektor.testrun.attributes.TestRunSystemAttributesRepository
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import projektor.incomingresults.model.PerformanceResult as IncomingPerformanceResult
import projektor.parser.model.TestCase as ParsedTestCase
import projektor.parser.model.TestSuite as ParsedTestSuite

/**
 * Object-store behavior when writes fail partway or run concurrently: retrying deletes, repairing the indexes,
 * and batching timeline updates.
 */
class ObjectStoreResilienceTest {
    private val config = ObjectStoreConfig("http://localhost:9000", "minio_access_key", "minio_secret_key")
    private val meterRegistry = SimpleMeterRegistry()
    private val client = FailingWritesClient(config, ObjectStoreTelemetry(meterRegistry))

    private lateinit var store: JsonObjectStore
    private lateinit var koin: Koin

    private val testRunRepository: TestRunRepository by lazy { koin.get() }
    private val repositoryTestRunRepository: RepositoryTestRunRepository by lazy { koin.get() }
    private val testRunIndex: TestRunIndex by lazy { koin.get() }

    @BeforeEach
    fun createStore() {
        client.createBucketIfNotExists(BUCKET_NAME)
        store = JsonObjectStore(client, BUCKET_NAME, "resilience-${UUID.randomUUID()}")
        koin = createKoin(store)
    }

    @Test
    fun `cleanup should retry a delete that failed after marking the test run deleted`() {
        val repoName = randomRepoName()
        val publicId = saveTestRun(repoName, Instant.parse("2020-05-01T10:00:00Z"))

        // As if the server stopped right after the first step of deleting the test run
        store.update<TestRunDocument>(ObjectStoreKeys.testRun(publicId)) { it!!.copy(deleted = true) }

        expectThat(runBlocking { testRunRepository.fetchTestRun(publicId) }).isNull()
        expectThat(findCleanupCandidates()).containsExactly(publicId)

        runBlocking { testRunRepository.deleteTestRun(publicId) }

        expectThat(store.list(ObjectStoreKeys.testRunPrefix(publicId)).objectNames).isEmpty()
        expectThat(runBlocking { repositoryTestRunRepository.fetchRepositoryTestRunTimeline(repoName, null) }).isNull()
        expectThat(findCleanupCandidates()).isEmpty()
        expectThat(store.list("created/").objectNames).isEmpty()
    }

    @Test
    fun `cleanup should remove a created marker left behind by a delete that failed before removing it`() {
        val createdTimestamp = Instant.parse("2020-05-01T10:00:00Z")
        val publicId = saveTestRun(randomRepoName(), createdTimestamp)

        runBlocking { testRunRepository.deleteTestRun(publicId) }
        // As if the server stopped before the last step of deleting the test run
        store.write(TestRunIndex.createdMarkerKey(publicId, createdTimestamp), CreatedMarkerDocument(publicId.id, createdTimestamp))

        expectThat(findCleanupCandidates()).isEmpty()
        expectThat(store.list("created/").objectNames).isEmpty()
    }

    @Test
    fun `repair should remove the timeline entry of a deleted test run when removing it failed`() {
        val repoName = randomRepoName()
        val publicId = saveTestRun(repoName)

        client.failWritesContaining = "/timeline/"
        runBlocking { testRunRepository.deleteTestRun(publicId) }
        client.failWritesContaining = null

        // The delete finished, only its timeline entry is left
        expectThat(runBlocking { testRunRepository.fetchTestRun(publicId) }).isNull()
        expectThat(timelinePublicIds(repoName)).containsExactly(publicId.id)
        expectThat(store.list(TestRunIndex.indexRepairPrefix()).objectNames.size).isEqualTo(1)

        expectThat(runBlocking { testRunIndex.repairIndex() }).isEqualTo(1)

        expectThat(timelinePublicIds(repoName)).isEmpty()
        expectThat(store.list(TestRunIndex.indexRepairPrefix()).objectNames).isEmpty()
    }

    @Test
    fun `saving a test run should succeed and be indexed by the repair job when indexing fails`() {
        val repoName = randomRepoName()

        client.failWritesContaining = "/timeline/"
        val publicId = saveTestRun(repoName)
        client.failWritesContaining = null

        expectThat(runBlocking { testRunRepository.fetchTestRun(publicId) }).isNotNull()
        expectThat(timelinePublicIds(repoName)).isEmpty()

        expectThat(runBlocking { testRunIndex.repairIndex() }).isEqualTo(1)

        expectThat(timelinePublicIds(repoName)).containsExactly(publicId.id)
        expectThat(findCleanupCandidates(LocalDate.now().plusDays(1))).containsExactly(publicId)
        expectThat(store.list(TestRunIndex.indexRepairPrefix()).objectNames).isEmpty()
    }

    @Test
    fun `repair should keep the marker of a test run that may still be saving, then drop it once abandoned`() {
        val publicId = randomPublicId()

        fun writeSavingMarker(markedAt: Instant) =
            store.write(
                "${TestRunIndex.indexRepairPrefix()}${publicId.id}-${UUID.randomUUID()}.json",
                IndexRepairDocument(publicId.id, randomRepoName(), Instant.now(), markedAt, savingTestRun = true),
            )

        writeSavingMarker(Instant.now())
        expectThat(runBlocking { testRunIndex.repairIndex() }).isEqualTo(0)
        expectThat(store.list(TestRunIndex.indexRepairPrefix()).objectNames.size).isEqualTo(1)

        store.deletePrefix(TestRunIndex.indexRepairPrefix())
        writeSavingMarker(Instant.now().minus(TestRunIndex.MISSING_TEST_RUN_REPAIR_DELAY).minusSeconds(60))
        expectThat(runBlocking { testRunIndex.repairIndex() }).isEqualTo(1)
        expectThat(store.list(TestRunIndex.indexRepairPrefix()).objectNames).isEmpty()
    }

    @Test
    fun `saving a test run should not leave a repair marker`() {
        saveTestRun(randomRepoName())

        expectThat(store.list(TestRunIndex.indexRepairPrefix()).objectNames).isEmpty()
    }

    @Test
    fun `many concurrent uploads to the same repository month should all be indexed without exhausting updates`() {
        val repoName = randomRepoName()
        val coverageRepository: CoverageRepository = koin.get()
        val performanceResultsRepository: PerformanceResultsRepository = koin.get()
        val testRunMetadataRepository: TestRunMetadataRepository = koin.get()
        val publicIds = (1..UPLOAD_COUNT).map { randomPublicId() }

        // Each upload updates the timeline shard as many times as a real upload with coverage and performance results
        runBlocking {
            publicIds.mapIndexed { uploadIdx, publicId ->
                async(Dispatchers.IO) {
                    testRunRepository.saveGroupedTestRun(publicId, groupedResults(repoName))
                    coverageRepository.createOrGetCoverageRun(publicId)
                    repeat(3) { reportIdx -> coverageRepository.addCoverageReport(publicId, coverageReport("report$reportIdx"), listOf()) }
                    repeat(
                        2,
                    ) { resultIdx -> performanceResultsRepository.savePerformanceResults(publicId, performanceResult("perf$resultIdx")) }
                    testRunMetadataRepository.updateGitMetadata(publicId, uploadIdx + 1, "sha$uploadIdx")
                }
            }.awaitAll()
        }

        val entries = runBlocking { testRunIndex.readRepositoryEntries(repoName) }
        expectThat(entries.map { it.publicId }.toSet()).isEqualTo(publicIds.map { it.id }.toSet())
        expectThat(entries.all { it.coverageStats?.lineCovered == 9 && it.performanceResults.size == 2 }).isEqualTo(true)
        expectThat(entries.map { it.pullRequestNumber }.toSet()).isEqualTo((1..UPLOAD_COUNT).toSet())

        expectThat(meterRegistry.find("object_store_update_exhausted").counter()?.count() ?: 0.0).isEqualTo(0.0)
        expectThat(store.list(TestRunIndex.indexRepairPrefix()).objectNames).isEmpty()
    }

    @Test
    fun `cleanup should ignore unexpected keys in the created date index`() {
        val publicId = saveTestRun(randomRepoName(), Instant.parse("2020-05-01T10:00:00Z"))
        store.write("created/not-a-date/stray.json", CreatedMarkerDocument("stray", Instant.EPOCH))
        store.write("created/stray.json", CreatedMarkerDocument("stray", Instant.EPOCH))

        expectThat(findCleanupCandidates()).containsExactly(publicId)
    }

    @Test
    fun `pinning a test run that is being deleted should do nothing`() {
        val systemAttributesRepository: TestRunSystemAttributesRepository = koin.get()
        val publicId = saveTestRun(randomRepoName())
        store.update<TestRunDocument>(ObjectStoreKeys.testRun(publicId)) { it!!.copy(deleted = true) }

        expectThat(runBlocking { systemAttributesRepository.pin(publicId) }).isEqualTo(0)

        runBlocking { testRunRepository.deleteTestRun(publicId) }
        expectThat(store.list(ObjectStoreKeys.testRunPrefix(publicId)).objectNames).isEmpty()
    }

    @Test
    fun `deleting a test run with an empty key prefix should not touch attachment objects in the same bucket`() {
        val sharedStore = JsonObjectStore(client, SHARED_BUCKET_NAME, "")
        client.createBucketIfNotExists(SHARED_BUCKET_NAME)
        val sharedKoin = createKoin(sharedStore)
        val sharedTestRunRepository: TestRunRepository = sharedKoin.get()

        val publicId = randomPublicId()
        runBlocking { sharedTestRunRepository.saveGroupedTestRun(publicId, groupedResults(randomRepoName())) }
        // Attachments are stored at the bucket root, named after the test run (see AttachmentService)
        val attachmentObjectName = "${publicId.id}-screenshot.png"
        client.putObject(SHARED_BUCKET_NAME, attachmentObjectName, "png".toByteArray(), "image/png")

        runBlocking { sharedTestRunRepository.deleteTestRun(publicId) }

        expectThat(sharedStore.list(ObjectStoreKeys.testRunPrefix(publicId)).objectNames).isEmpty()
        expectThat(client.getObjectWithETag(SHARED_BUCKET_NAME, attachmentObjectName)).isNotNull()

        client.removeObject(SHARED_BUCKET_NAME, attachmentObjectName)
    }

    private fun createKoin(jsonObjectStore: JsonObjectStore) =
        koinApplication {
            modules(
                createObjectStorePersistenceModule(
                    jsonObjectStore,
                    PersistenceInstrumentation(PersistenceType.OBJECT_STORE, SimpleMeterRegistry()),
                ),
            )
        }.koin

    private fun saveTestRun(
        repoName: String,
        createdTimestamp: Instant? = null,
    ): PublicId {
        val publicId = randomPublicId()
        runBlocking { testRunRepository.saveGroupedTestRun(publicId, groupedResults(repoName, createdTimestamp)) }
        return publicId
    }

    private fun findCleanupCandidates(createdBefore: LocalDate = LocalDate.parse("2020-05-02")) =
        runBlocking { testRunRepository.findTestRunsCreatedBeforeAndNotPinned(createdBefore) }

    private fun timelinePublicIds(repoName: String) = runBlocking { testRunIndex.readRepositoryEntries(repoName) }.map { it.publicId }

    private fun randomRepoName() = "org-${UUID.randomUUID()}/repo"

    private fun groupedResults(
        repoName: String,
        createdTimestamp: Instant? = null,
    ) = GroupedResults(
        listOf(GroupedTestSuites(listOf(createTestSuite()), null, null, null)),
        listOf(),
        ResultsMetadata(GitMetadata(repoName, null, "main", true), true, null, createdTimestamp),
        null,
        null,
        null,
    )

    private fun createTestSuite(): ParsedTestSuite {
        val testSuite = ParsedTestSuite()
        testSuite.name = "Suite"
        testSuite.tests = 1
        testSuite.time = BigDecimal("1.5")
        testSuite.testCases =
            listOf(
                ParsedTestCase().apply {
                    name = "test1"
                    className = "Suite"
                    time = BigDecimal("1.5")
                },
            )
        return testSuite
    }

    private fun coverageReport(name: String) =
        CoverageReport(name, CoverageReportStats(CoverageReportStat(1, 1), CoverageReportStat(3, 1), CoverageReportStat(1, 1)), null)

    private fun performanceResult(name: String) =
        IncomingPerformanceResult(
            name = name,
            requestsPerSecond = BigDecimal("10.5"),
            requestCount = 100,
            average = BigDecimal("1.5"),
            maximum = BigDecimal("3.0"),
            p95 = BigDecimal("2.5"),
        )

    /**
     * Fails writes to keys containing the given text, like a store that's unavailable or a conflict that never ends.
     */
    private class FailingWritesClient(config: ObjectStoreConfig, telemetry: ObjectStoreTelemetry) : ObjectStoreClient(config, telemetry) {
        @Volatile
        var failWritesContaining: String? = null

        override fun putObject(
            bucketName: String,
            objectName: String,
            bytes: ByteArray,
            contentType: String,
            condition: WriteCondition,
        ): String {
            failWritesContaining?.let { text ->
                if (objectName.contains(text)) throw IllegalStateException("Simulated failure writing $objectName")
            }

            return super.putObject(bucketName, objectName, bytes, contentType, condition)
        }
    }

    companion object {
        private const val BUCKET_NAME = "persistenceresilience"
        private const val SHARED_BUCKET_NAME = "persistencesharedbucket"

        private const val UPLOAD_COUNT = 30
    }
}
