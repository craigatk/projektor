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
import projektor.attachment.AttachmentRepository
import projektor.coverage.CoverageRepository
import projektor.incomingresults.model.GitMetadata
import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.incomingresults.model.ResultsMetadata
import projektor.incomingresults.randomPublicId
import projektor.metadata.TestRunMetadataRepository
import projektor.objectstore.ObjectDeletionException
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.objectstore.ObjectStoreTelemetry
import projektor.objectstore.PreconditionFailedException
import projektor.objectstore.StoredObject
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
import projektor.server.api.attachments.Attachment
import projektor.testrun.TestRunObjectStoreRepository
import projektor.testrun.TestRunRepository
import projektor.testrun.attributes.TestRunSystemAttributesRepository
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
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
    fun `a delete that fails partway should keep the test run document so cleanup retries it`() {
        val publicId = saveTestRun(randomRepoName(), Instant.parse("2020-05-01T10:00:00Z"))

        client.failObjectDeletes = true
        expectThrows<ObjectDeletionException> { testRunRepository.deleteTestRun(publicId) }
        client.failObjectDeletes = false

        expectThat(store.exists(ObjectStoreKeys.testRun(publicId))).isTrue()
        expectThat(findCleanupCandidates()).containsExactly(publicId)

        runBlocking { testRunRepository.deleteTestRun(publicId) }

        expectThat(store.list(ObjectStoreKeys.testRunPrefix(publicId)).objectNames).isEmpty()
        expectThat(store.list("created/").objectNames).isEmpty()
    }

    @Test
    fun `an unreadable document should only leave its own test run out of the cleanup scan`() {
        val createdTimestamp = Instant.parse("2020-05-01T10:00:00Z")
        val readable = saveTestRun(randomRepoName(), createdTimestamp)
        val unreadable = saveTestRun(randomRepoName(), createdTimestamp)
        store.write(ObjectStoreKeys.systemAttributes(unreadable), SystemAttributesDocument(false))

        client.failReadsContaining = ObjectStoreKeys.systemAttributes(unreadable)

        expectThat(findCleanupCandidates()).containsExactly(readable)
    }

    @Test
    fun `repair should remove the documents of a save that never finished, but only once abandoned`() {
        val publicId = randomPublicId()

        client.failWritesContaining = ObjectStoreKeys.testRun(publicId)
        expectThrows<IllegalStateException> { testRunRepository.saveGroupedTestRun(publicId, groupedResults(randomRepoName())) }
        client.failWritesContaining = null

        expectThat(store.list(ObjectStoreKeys.testRunPrefix(publicId)).objectNames).isNotEmpty()

        runBlocking { testRunIndex.repairIndex() }
        expectThat(store.list(ObjectStoreKeys.testRunPrefix(publicId)).objectNames).isNotEmpty()

        val markerKey = store.list(TestRunIndex.indexRepairPrefix()).objectNames.single()
        store.update<IndexRepairDocument>(markerKey) {
            it!!.copy(markedAt = Instant.now().minus(TestRunIndex.MISSING_TEST_RUN_REPAIR_DELAY).minusSeconds(60))
        }

        runBlocking { testRunIndex.repairIndex() }

        expectThat(store.list(ObjectStoreKeys.testRunPrefix(publicId)).objectNames).isEmpty()
        expectThat(store.list(TestRunIndex.indexRepairPrefix()).objectNames).isEmpty()
    }

    @Test
    fun `a test run whose save committed but looked like a conflict should still get indexed`() {
        val publicId = randomPublicId()
        val repoName = randomRepoName()

        client.conflictAfterCommitting = ObjectStoreKeys.testRun(publicId) to 0
        expectThrows<IllegalStateException> { testRunRepository.saveGroupedTestRun(publicId, groupedResults(repoName)) }

        runBlocking { testRunIndex.repairIndex() }

        expectThat(timelinePublicIds(repoName)).containsExactly(publicId.id)
        expectThat(store.list("created/").objectNames).hasSize(1)
        expectThat(store.list(TestRunIndex.indexRepairPrefix()).objectNames).isEmpty()
    }

    @Test
    fun `an append that commits but looks like a conflict should not add its test suites twice`() {
        val publicId = saveTestRun(randomRepoName())

        // The first write reserves the indexes and the second adds the test suites
        client.conflictAfterCommitting = ObjectStoreKeys.testRun(publicId) to 1

        runBlocking {
            testRunRepository.appendTestSuites(publicId, 2, listOf(GroupedTestSuites(listOf(createTestSuite()), null, null, null)))
        }

        val testRun = runBlocking { testRunRepository.fetchTestRun(publicId) }
        expectThat(testRun?.testSuites?.map { it.idx }).isEqualTo(listOf(1, 3))
    }

    @Test
    fun `deleting a test run should remove its attachments document only when it is empty`() {
        val attachmentRepository: AttachmentRepository = koin.get()
        val emptied = saveTestRun(randomRepoName())
        val withAttachment = saveTestRun(randomRepoName())

        runBlocking {
            attachmentRepository.addAttachment(emptied, Attachment("a.txt", "a", 1))
            attachmentRepository.deleteAttachment(emptied, "a")
            attachmentRepository.addAttachment(withAttachment, Attachment("b.txt", "b", 1))

            testRunRepository.deleteTestRun(emptied)
            testRunRepository.deleteTestRun(withAttachment)
        }

        expectThat(store.exists(ObjectStoreKeys.attachments(emptied))).isFalse()
        expectThat(store.exists(ObjectStoreKeys.attachments(withAttachment))).isTrue()
    }

    @Test
    fun `should find old coverage and attachments without a test run, and leave ones with a test run or recently written`() {
        val attachmentRepository: AttachmentRepository = koin.get()
        val coverageRepository: CoverageRepository = koin.get()
        val orphanedFinder = TestRunObjectStoreRepository(koin.get(), koin.get(), orphanedDataMinAge = Duration.ZERO)

        val saved = saveTestRun(randomRepoName())
        val coverageOnly = randomPublicId()
        val attachmentOnly = randomPublicId()

        runBlocking {
            coverageRepository.createOrGetCoverageRun(saved)
            coverageRepository.createOrGetCoverageRun(coverageOnly)
            attachmentRepository.addAttachment(attachmentOnly, Attachment("a.txt", "a", 1))
        }

        expectThat(runBlocking { orphanedFinder.findOrphanedTestRunData() }.map { it.id }.toSet())
            .isEqualTo(setOf(coverageOnly.id, attachmentOnly.id))
        // They could still be waiting for their test run to finish saving
        expectThat(runBlocking { testRunRepository.findOrphanedTestRunData() }).isEmpty()

        runBlocking {
            coverageRepository.deleteCoverage(coverageOnly)
            attachmentRepository.deleteAttachment(attachmentOnly, "a")
            testRunRepository.deleteTestRun(coverageOnly)
            testRunRepository.deleteTestRun(attachmentOnly)
        }

        expectThat(runBlocking { orphanedFinder.findOrphanedTestRunData() }).isEmpty()
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

    @Test
    fun `cleanup scan should not read the documents of test runs it finds`() {
        val publicIds =
            listOf("2020-05-01T10:00:00Z", "2020-05-01T11:00:00Z", "2020-04-20T10:00:00Z").map {
                saveTestRun(randomRepoName(), Instant.parse(it))
            }

        client.readKeys.clear()
        // The last day's markers are read to compare their timestamps, so look past these test runs' days
        expectThat(findCleanupCandidates(LocalDate.parse("2020-05-03")).toSet()).isEqualTo(publicIds.toSet())

        expectThat(client.readKeys.filter { it.contains("/created/") || it.endsWith("/run.json") }).isEmpty()
    }

    @Test
    fun `attachment cleanup scan should only read documents of test runs with attachments`() {
        val attachmentRepository: AttachmentRepository = koin.get()
        val withAttachment = saveTestRun(randomRepoName(), Instant.parse("2020-05-01T10:00:00Z"))
        val withoutAttachment = saveTestRun(randomRepoName(), Instant.parse("2020-05-01T11:00:00Z"))
        val attachmentsRemoved = saveTestRun(randomRepoName(), Instant.parse("2020-05-01T12:00:00Z"))

        runBlocking {
            attachmentRepository.addAttachment(withAttachment, Attachment("a.txt", "a", 1))
            attachmentRepository.addAttachment(attachmentsRemoved, Attachment("b.txt", "b", 1))
            attachmentRepository.deleteAttachment(attachmentsRemoved, "b")
        }

        client.readKeys.clear()
        val candidates =
            runBlocking { testRunRepository.findTestRunsCreatedBeforeAndNotPinnedWithAttachments(LocalDate.parse("2020-05-03")) }

        expectThat(candidates).containsExactly(withAttachment)
        expectThat(client.readKeys.filter { it.contains(withoutAttachment.id) }).isEmpty()
    }

    @Test
    fun `cleanup scan should skip pinned test runs and keep runs created after the cutoff`() {
        val systemAttributesRepository: TestRunSystemAttributesRepository = koin.get()
        val old = saveTestRun(randomRepoName(), Instant.parse("2020-05-01T10:00:00Z"))
        val pinned = saveTestRun(randomRepoName(), Instant.parse("2020-05-01T11:00:00Z"))
        saveTestRun(randomRepoName(), Instant.parse("2020-05-02T00:00:01Z"))
        runBlocking { systemAttributesRepository.pin(pinned) }

        expectThat(findCleanupCandidates(LocalDate.parse("2020-05-02"))).containsExactly(old)
    }

    @Test
    fun `recent run lookups should stop reading shards once they have enough`() {
        val repoName = randomRepoName()
        val oldest = saveTestRun(repoName, Instant.parse("2020-01-10T10:00:00Z"))
        val middle = saveTestRun(repoName, Instant.parse("2020-02-10T10:00:00Z"))
        val newest = saveTestRun(repoName, Instant.parse("2020-03-10T10:00:00Z"))

        fun timelineShardsRead() = client.readKeys.filter { it.contains("/timeline/") }.map { it.substringAfterLast('/') }

        client.readKeys.clear()
        expectThat(runBlocking { repositoryTestRunRepository.fetchRecentTestRunPublicIds(repoName, null, 1) }).containsExactly(newest)
        expectThat(timelineShardsRead()).containsExactly("2020-03.json")

        client.readKeys.clear()
        expectThat(
            runBlocking { repositoryTestRunRepository.fetchRecentTestRunPublicIds(repoName, null, 2) },
        ).containsExactly(newest, middle)
        expectThat(timelineShardsRead()).containsExactly("2020-03.json", "2020-02.json")

        client.readKeys.clear()
        expectThat(runBlocking { repositoryTestRunRepository.fetchRecentTestRunPublicIds(repoName, null, 10) })
            .containsExactly(newest, middle, oldest)
        expectThat(timelineShardsRead()).containsExactly("2020-03.json", "2020-02.json", "2020-01.json")
    }

    @Test
    fun `reading entries before a test run should skip newer months and keep save order for equal timestamps`() {
        val repoName = randomRepoName()
        val sharedTimestamp = Instant.parse("2020-02-10T10:00:00Z")
        val older = saveTestRun(repoName, Instant.parse("2020-01-10T10:00:00Z"))
        val firstSaved = saveTestRun(repoName, sharedTimestamp)
        val target = saveTestRun(repoName, sharedTimestamp)
        saveTestRun(repoName, sharedTimestamp)
        saveTestRun(repoName, Instant.parse("2020-03-10T10:00:00Z"))

        fun entriesBefore(includeEntry: Boolean) =
            runBlocking {
                testRunIndex.readNewestEntriesBefore(repoName, target, sharedTimestamp, includeEntry, 10) { true }
            }?.map { it.publicId }

        expectThat(entriesBefore(includeEntry = false)).isEqualTo(listOf(firstSaved.id, older.id))
        expectThat(entriesBefore(includeEntry = true)).isEqualTo(listOf(target.id, firstSaved.id, older.id))

        // A test run missing from the timeline, such as one still waiting to be indexed
        expectThat(
            runBlocking { testRunIndex.readNewestEntriesBefore(repoName, randomPublicId(), sharedTimestamp, true, 10) { true } },
        ).isNull()
    }

    @Test
    fun `concurrent appends to a test run within a server should not conflict`() {
        val publicId = saveTestRun(randomRepoName())

        runBlocking {
            (1..UPLOAD_COUNT).map {
                async(Dispatchers.IO) {
                    testRunRepository.appendTestSuites(publicId, 1, listOf(GroupedTestSuites(listOf(createTestSuite()), null, null, null)))
                    testRunRepository.updateTestRunSummary(publicId, null)
                }
            }.awaitAll()
        }

        val summary = runBlocking { testRunRepository.fetchTestRunSummary(publicId) }
        expectThat(summary?.totalTestCount).isEqualTo(UPLOAD_COUNT + 1)
        expectThat(meterRegistry.find("object_store_update_conflict").counter()?.count() ?: 0.0).isEqualTo(0.0)
    }

    @Test
    fun `concurrent appends from different server instances should all be kept`() {
        val otherInstanceRepository: TestRunRepository = createKoin(store).get()
        val publicId = saveTestRun(randomRepoName())

        runBlocking {
            (1..UPLOAD_COUNT).map { uploadIdx ->
                val repository = if (uploadIdx % 2 == 0) testRunRepository else otherInstanceRepository

                async(Dispatchers.IO) {
                    repository.appendTestSuites(publicId, 1, listOf(GroupedTestSuites(listOf(createTestSuite()), null, null, null)))
                    repository.updateTestRunSummary(publicId, null)
                }
            }.awaitAll()
        }

        val summary = runBlocking { testRunRepository.fetchTestRunSummary(publicId) }
        expectThat(summary?.totalTestCount).isEqualTo(UPLOAD_COUNT + 1)
        expectThat(meterRegistry.find("object_store_update_exhausted").counter()?.count() ?: 0.0).isEqualTo(0.0)
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

        @Volatile
        var failReadsContaining: String? = null

        @Volatile
        var failObjectDeletes = false

        // Key whose conditional write commits and then fails its condition, like the retry of a write that committed,
        // after this many other conditional writes to the key
        @Volatile
        var conflictAfterCommitting: Pair<String, Int>? = null

        // Every object read, in order, with its full key
        val readKeys: MutableCollection<String> = ConcurrentLinkedQueue()

        override fun getObjectWithETag(
            bucketName: String,
            objectName: String,
        ): StoredObject? {
            readKeys.add(objectName)

            failReadsContaining?.let { text ->
                if (objectName.contains(text)) throw IllegalStateException("Simulated failure reading $objectName")
            }

            return super.getObjectWithETag(bucketName, objectName)
        }

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

            val etag = super.putObject(bucketName, objectName, bytes, contentType, condition)

            conflictAfterCommitting?.let { (key, writesBefore) ->
                if (objectName.endsWith(key) && condition != WriteCondition.None) {
                    if (writesBefore > 0) {
                        conflictAfterCommitting = key to writesBefore - 1
                    } else {
                        conflictAfterCommitting = null
                        throw PreconditionFailedException(bucketName, objectName, condition, IllegalStateException("Simulated"))
                    }
                }
            }

            return etag
        }

        override fun removeObjects(
            bucketName: String,
            objectNames: List<String>,
        ) {
            if (failObjectDeletes) throw ObjectDeletionException(bucketName, objectNames)

            super.removeObjects(bucketName, objectNames)
        }
    }

    companion object {
        private const val BUCKET_NAME = "persistenceresilience"
        private const val SHARED_BUCKET_NAME = "persistencesharedbucket"

        private const val UPLOAD_COUNT = 30
    }
}
