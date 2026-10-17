package projektor.persistence.objectstore

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
import projektor.error.ProcessingFailureRepository
import projektor.incomingresults.model.GitMetadata
import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.incomingresults.model.ResultsMetadata
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.incomingresults.randomPublicId
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.objectstore.json.JsonObjectStore
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats
import projektor.parser.model.Failure
import projektor.performance.PerformanceResultsRepository
import projektor.repository.coverage.RepositoryCoverageRepository
import projektor.repository.performance.RepositoryPerformanceRepository
import projektor.server.api.attachments.Attachment
import projektor.server.api.error.FailureBodyType
import projektor.server.api.repository.BranchType
import projektor.testcase.TestCaseRepository
import projektor.testrun.TestRunRepository
import projektor.testrun.attributes.TestRunSystemAttributesRepository
import projektor.testsuite.TestSuiteRepository
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import projektor.incomingresults.model.PerformanceResult as IncomingPerformanceResult
import projektor.parser.model.TestCase as ParsedTestCase
import projektor.parser.model.TestSuite as ParsedTestSuite

/**
 * Behavior specific to the object-store backend, which ObjectStoreParityTest can't cover by comparing with the database.
 */
class ObjectStoreRepositoriesTest {
    private val objectStoreClient = ObjectStoreClient(ObjectStoreConfig("http://localhost:9000", "minio_access_key", "minio_secret_key"))

    private lateinit var jsonObjectStore: JsonObjectStore
    private lateinit var koin: Koin

    private val testRunRepository: TestRunRepository by lazy { koin.get() }
    private val testSuiteRepository: TestSuiteRepository by lazy { koin.get() }
    private val testCaseRepository: TestCaseRepository by lazy { koin.get() }

    @BeforeEach
    fun createStore() {
        objectStoreClient.createBucketIfNotExists(BUCKET_NAME)
        jsonObjectStore = JsonObjectStore(objectStoreClient, BUCKET_NAME, "repositories-${UUID.randomUUID()}")
        koin = koinApplication { modules(createObjectStorePersistenceModule(jsonObjectStore)) }.koin
    }

    @Test
    fun `should read test suites and test cases from every chunk of a large test run`() {
        val publicId = randomPublicId()
        val testSuiteCount = TestRunDocumentStore.TEST_SUITES_PER_CHUNK * 2 + 50

        runBlocking {
            testRunRepository.saveGroupedTestRun(
                publicId,
                groupedResults(
                    GroupedTestSuites((1..testSuiteCount).map { createTestSuite("Suite$it", failing = it % 50 == 0) }, "Group", null, null),
                ),
            )
        }

        val testSuites = runBlocking { testSuiteRepository.fetchTestSuitesWithCases(publicId) }
        expectThat(testSuites.map { it.idx }).isEqualTo((1..testSuiteCount).toList())
        expectThat(testSuites.all { it.testCases?.size == 2 }).isTrue()

        val lastTestSuite = runBlocking { testSuiteRepository.fetchTestSuite(publicId, testSuiteCount) }
        expectThat(lastTestSuite).isNotNull().and {
            get { className }.isEqualTo("Suite$testSuiteCount")
            get { testCases?.map { it.name } }.isEqualTo(listOf("Suite$testSuiteCount-test1", "Suite$testSuiteCount-test2"))
        }

        val testCaseInMiddleChunk = runBlocking { testCaseRepository.fetchTestCase(publicId, 150, 2) }
        expectThat(testCaseInMiddleChunk?.name).isEqualTo("Suite150-test2")

        val failedTestCases = runBlocking { testCaseRepository.fetchFailedTestCases(publicId) }
        expectThat(failedTestCases.map { it.testSuiteName }).containsExactly("Suite50", "Suite100", "Suite150", "Suite200", "Suite250")
    }

    @Test
    fun `concurrent appends should get distinct test suite indexes`() {
        val publicId = randomPublicId()
        runBlocking {
            testRunRepository.saveGroupedTestRun(
                publicId,
                groupedResults(GroupedTestSuites(listOf(createTestSuite("Initial")), "Group", null, null)),
            )
        }

        val appendCount = 5
        runBlocking {
            (1..appendCount).map { appendIdx ->
                async(Dispatchers.IO) {
                    // Every append starts from the same highest index, like parallel CI jobs appending to the same group
                    testRunRepository.appendTestSuites(
                        publicId,
                        1,
                        listOf(GroupedTestSuites(listOf(createTestSuite("Appended$appendIdx")), "Group", null, null)),
                    )
                }
            }.awaitAll()
        }

        val testSuites = runBlocking { testSuiteRepository.fetchTestSuitesWithCases(publicId) }
        expectThat(testSuites).hasSize(appendCount + 1)
        expectThat(testSuites.map { it.idx }.distinct()).hasSize(appendCount + 1)
        expectThat(testSuites.map { it.className }.toSet()).isEqualTo(setOf("Initial") + (1..appendCount).map { "Appended$it" })
        expectThat(testSuites.all { it.testCases?.size == 2 }).isTrue()
    }

    @Test
    fun `deleting test run should keep its coverage, attachments and results processing`() {
        val publicId = randomPublicId()
        val attachmentRepository: AttachmentRepository = koin.get()
        val coverageRepository: CoverageRepository = koin.get()
        val resultsProcessingRepository: ResultsProcessingRepository = koin.get()
        val systemAttributesRepository: TestRunSystemAttributesRepository = koin.get()

        runBlocking {
            resultsProcessingRepository.createResultsProcessing(publicId)
            testRunRepository.saveGroupedTestRun(
                publicId,
                groupedResults(GroupedTestSuites(listOf(createTestSuite("Suite")), null, null, null)),
            )
            systemAttributesRepository.pin(publicId)
            attachmentRepository.addAttachment(publicId, Attachment("screenshot.png", "object-1", 100L))
            coverageRepository.addCoverageReport(
                publicId,
                CoverageReport(
                    "report",
                    CoverageReportStats(CoverageReportStat(1, 1), CoverageReportStat(1, 1), CoverageReportStat(1, 1)),
                    null,
                ),
                listOf(),
            )

            testRunRepository.deleteTestRun(publicId)
        }

        expectThat(runBlocking { testRunRepository.fetchTestRun(publicId) }).isNull()
        expectThat(runBlocking { testCaseRepository.fetchTestCases(publicId) }).hasSize(0)
        expectThat(runBlocking { systemAttributesRepository.fetchAttributes(publicId) }).isNull()

        expectThat(runBlocking { attachmentRepository.listAttachments(publicId) }).hasSize(1)
        expectThat(runBlocking { coverageRepository.coverageGroupExists(publicId) }).isTrue()
        expectThat(runBlocking { resultsProcessingRepository.fetchResultsProcessing(publicId) }).isNotNull()

        expectThat(runBlocking { coverageRepository.deleteCoverage(publicId) }).isTrue()
        expectThat(runBlocking { coverageRepository.coverageGroupExists(publicId) }).isFalse()
    }

    @Test
    fun `should fetch most recent processing failures first`() {
        val processingFailureRepository: ProcessingFailureRepository = koin.get()
        val publicIds = (1..4).map { randomPublicId() }

        publicIds.forEach { publicId ->
            runBlocking {
                processingFailureRepository.recordProcessingFailure(
                    publicId,
                    "body-${publicId.id}",
                    FailureBodyType.TEST_RESULTS,
                    "failed",
                )
            }
            // Failures are ordered by millisecond timestamp
            Thread.sleep(5)
        }

        val recentFailures = runBlocking { processingFailureRepository.fetchRecentProcessingFailures(3) }

        expectThat(recentFailures.map { it.id }).containsExactly(publicIds[3].id, publicIds[2].id, publicIds[1].id)
        expectThat(recentFailures[0].body).isEqualTo("body-${publicIds[3].id}")
    }

    @Test
    fun `should save test run with the same public ID only once`() {
        val publicId = randomPublicId()
        val results = groupedResults(GroupedTestSuites(listOf(createTestSuite("Suite")), null, null, null))

        runBlocking { testRunRepository.saveGroupedTestRun(publicId, results) }

        val secondSave = runCatching { runBlocking { testRunRepository.saveGroupedTestRun(publicId, results) } }
        expectThat(secondSave.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `repository performance timeline should include every performance result of a run`() {
        val publicId = randomPublicId()
        val repoName = "org-${UUID.randomUUID()}/repo"
        val performanceResultsRepository: PerformanceResultsRepository = koin.get()
        val repositoryPerformanceRepository: RepositoryPerformanceRepository = koin.get()

        runBlocking {
            testRunRepository.saveGroupedTestRun(
                publicId,
                groupedResults(repoName, GroupedTestSuites(listOf(createTestSuite("Suite")), null, null, null)),
            )
            listOf("first", "second").forEach { name ->
                performanceResultsRepository.savePerformanceResults(
                    publicId,
                    IncomingPerformanceResult(name, BigDecimal("1"), 1, BigDecimal("1"), BigDecimal("1"), BigDecimal("1")),
                )
            }
        }

        val timelineEntries = runBlocking { repositoryPerformanceRepository.fetchTestTimelineEntries(repoName, null) }
        expectThat(timelineEntries.map { it.performanceResult.name }).containsExactly("first", "second")
    }

    @Test
    fun `repository timeline should reflect every concurrent change to the same month`() {
        val repoName = "org-${UUID.randomUUID()}/repo"
        val coverageRepository: CoverageRepository = koin.get()
        val repositoryCoverageRepository: RepositoryCoverageRepository = koin.get()
        val publicIds = (1..6).map { randomPublicId() }

        runBlocking {
            // Every run lands in the same timeline shard, so the index updates contend with each other
            publicIds.map { publicId ->
                async(Dispatchers.IO) {
                    testRunRepository.saveGroupedTestRun(
                        publicId,
                        groupedResults(repoName, GroupedTestSuites(listOf(createTestSuite("Suite")), null, null, null)),
                    )
                    coverageRepository.addCoverageReport(
                        publicId,
                        CoverageReport(
                            "report",
                            CoverageReportStats(CoverageReportStat(1, 1), CoverageReportStat(3, 1), CoverageReportStat(1, 1)),
                            null,
                        ),
                        listOf(),
                    )
                }
            }.awaitAll()
        }

        val coverageTimeline = runBlocking { repositoryCoverageRepository.fetchRepositoryCoverageTimeline(BranchType.ALL, repoName, null) }
        expectThat(coverageTimeline?.timelineEntries?.map { it.publicId }?.toSet()).isEqualTo(publicIds.map { it.id }.toSet())
        expectThat(coverageTimeline?.timelineEntries?.all { it.coverageStats.lineStat.covered == 3 }).isEqualTo(true)
    }

    @Test
    fun `test run cleanup should find test runs without Git metadata and skip pinned runs`() {
        val withoutGit = randomPublicId()
        val withGit = randomPublicId()
        val pinned = randomPublicId()
        val systemAttributesRepository: TestRunSystemAttributesRepository = koin.get()
        val createdTimestamp = Instant.parse("2020-05-01T10:00:00Z")

        runBlocking {
            listOf(withoutGit, withGit, pinned).forEach { publicId ->
                val repoName = if (publicId == withoutGit) null else "org-${UUID.randomUUID()}/repo"
                testRunRepository.saveGroupedTestRun(
                    publicId,
                    groupedResults(
                        repoName,
                        GroupedTestSuites(listOf(createTestSuite("Suite")), null, null, null),
                        createdTimestamp = createdTimestamp,
                    ),
                )
            }
            systemAttributesRepository.pin(pinned)
        }

        expectThat(runBlocking { testRunRepository.findTestRunsCreatedBeforeAndNotPinned(LocalDate.parse("2020-05-01")) }).hasSize(0)
        expectThat(runBlocking { testRunRepository.findTestRunsCreatedBeforeAndNotPinned(LocalDate.parse("2020-05-02")).toSet() })
            .isEqualTo(setOf(withoutGit, withGit))

        runBlocking { testRunRepository.deleteTestRun(withoutGit) }
        expectThat(
            runBlocking { testRunRepository.findTestRunsCreatedBeforeAndNotPinned(LocalDate.parse("2020-05-02")) },
        ).containsExactly(withGit)
    }

    private fun groupedResults(
        repoName: String?,
        vararg groupedTestSuites: GroupedTestSuites,
        createdTimestamp: Instant? = null,
    ) = GroupedResults(
        groupedTestSuites.toList(),
        listOf(),
        ResultsMetadata(repoName?.let { GitMetadata(it, null, "main", true) }, true, null, createdTimestamp),
        null,
        null,
        null,
    )

    private fun groupedResults(vararg groupedTestSuites: GroupedTestSuites) =
        GroupedResults(groupedTestSuites.toList(), listOf(), null, null, null, null)

    private fun createTestSuite(
        name: String,
        failing: Boolean = false,
    ): ParsedTestSuite {
        val testSuite = ParsedTestSuite()
        testSuite.name = name
        testSuite.tests = 2
        testSuite.failures = if (failing) 1 else 0
        testSuite.time = BigDecimal("1.5")
        testSuite.testCases =
            (1..2).map { testCaseIdx ->
                val testCase = ParsedTestCase()
                testCase.name = "$name-test$testCaseIdx"
                testCase.className = name
                testCase.time = BigDecimal("0.75")
                if (failing && testCaseIdx == 1) {
                    testCase.failure = Failure().apply { message = "Failed" }
                }
                testCase
            }
        return testSuite
    }

    companion object {
        private const val BUCKET_NAME = "persistenceparity"
    }
}
