package projektor.persistence.objectstore

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import projektor.DatabaseRepositoryTestCase
import projektor.attachment.AttachmentRepository
import projektor.coverage.CoverageGroupStatus
import projektor.coverage.CoverageRepository
import projektor.incomingresults.model.GitMetadata
import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.incomingresults.model.ResultsMetadata
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.incomingresults.randomPublicId
import projektor.metadata.TestRunMetadataRepository
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.objectstore.json.JsonObjectStore
import projektor.parser.ResultsXmlLoader
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats
import projektor.performance.PerformanceResultsRepository
import projektor.persistence.createDatabasePersistenceModule
import projektor.quality.CodeQualityReportRepository
import projektor.results.processor.TestResultsProcessor
import projektor.server.api.PublicId
import projektor.server.api.attachments.Attachment
import projektor.server.api.coverage.CoverageFile
import projektor.server.api.coverage.CoverageStat
import projektor.server.api.coverage.CoverageStats
import projektor.server.api.quality.CodeQualityReport
import projektor.server.api.results.ResultsProcessingStatus
import projektor.testcase.TestCaseRepository
import projektor.testrun.TestRunRepository
import projektor.testrun.attributes.TestRunSystemAttributesRepository
import projektor.testsuite.TestSuiteRepository
import projektor.testsuite.TestSuiteSearchCriteria
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import projektor.incomingresults.model.PerformanceResult as IncomingPerformanceResult

/**
 * Saves the same data to both the database and object-store backends (under the same public ID,
 * since the backends are independent) and checks that every fetch returns equal results.
 */
class ObjectStoreParityTest : DatabaseRepositoryTestCase() {
    private val resultsXmlLoader = ResultsXmlLoader()
    private val testResultsProcessor = TestResultsProcessor()

    private lateinit var database: Repositories
    private lateinit var objectStore: Repositories

    @BeforeEach
    fun createBackends() {
        database = Repositories(koinApplication { modules(createDatabasePersistenceModule(dataSource, dslContext)) }.koin)

        val objectStoreClient = ObjectStoreClient(ObjectStoreConfig("http://localhost:9000", "minio_access_key", "minio_secret_key"))
        objectStoreClient.createBucketIfNotExists(BUCKET_NAME)
        val jsonObjectStore = JsonObjectStore(objectStoreClient, BUCKET_NAME, "parity-${UUID.randomUUID()}")
        objectStore = Repositories(koinApplication { modules(createObjectStorePersistenceModule(jsonObjectStore)) }.koin)
    }

    @Test
    fun `grouped test run should match`() {
        val publicId = randomPublicId()

        val groupedResults =
            GroupedResults(
                groupedTestSuites =
                    listOf(
                        GroupedTestSuites(
                            parse(resultsXmlLoader.passing(), resultsXmlLoader.failing(), resultsXmlLoader.output()),
                            "Group1",
                            "Label1",
                            "directory-1",
                        ),
                        GroupedTestSuites(
                            parse(
                                resultsXmlLoader.someIgnoredSomeFailing(),
                                resultsXmlLoader.slow(),
                                resultsXmlLoader.cypressAttachmentsSpecWithFilePath(),
                            ),
                            "Group2",
                            null,
                            null,
                        ),
                    ),
                performanceResults = listOf(),
                metadata =
                    ResultsMetadata(
                        git = GitMetadata("craigatk/projektor", "server", "main", true, "abc123", 42),
                        ci = true,
                        group = "B12",
                        // Nanoseconds, to check both backends round to the same precision
                        createdTimestamp = Instant.parse("2026-10-17T10:15:30.123456789Z"),
                    ),
                wallClockDuration = BigDecimal("12.3456"),
                coverageFiles = null,
                codeQualityReports = null,
            )

        expectSameResult { it.testRunRepository.saveGroupedTestRun(publicId, groupedResults) }

        expectSameTestRunData(publicId)

        // Guard against the comparisons passing because both backends return nothing
        val testCases = runBlocking { objectStore.testCaseRepository.fetchTestCases(publicId) }
        expectThat(testCases.size).isGreaterThan(5)
        expectThat(testCases.count { !it.passed }).isGreaterThan(0)
        expectThat(testCases.count { it.skipped }).isGreaterThan(0)
        expectThat(testCases.count { it.hasSystemOutTestCase || it.hasSystemOutTestSuite }).isGreaterThan(0)
        expectThat(testCases.mapNotNull { it.fileName }.size).isGreaterThan(0)
        val outputTestCase = testCases.first { it.hasSystemOut }
        val output =
            runBlocking {
                if (outputTestCase.hasSystemOutTestCase) {
                    objectStore.testCaseRepository.fetchTestCaseSystemOut(publicId, outputTestCase.testSuiteIdx, outputTestCase.idx)
                } else {
                    objectStore.testSuiteRepository.fetchTestSuiteSystemOut(publicId, outputTestCase.testSuiteIdx)
                }
            }
        expectThat(output.value.orEmpty().length).isGreaterThan(0)

        expectSameResult { it.testRunMetadataRepository.updateGitMetadata(publicId, 43, "def456") }
        expectSameResult { it.testRunMetadataRepository.fetchGitMetadata(publicId) }
    }

    @Test
    fun `ungrouped test run should match`() {
        val publicId = randomPublicId()

        expectSameResult(ignoringCreatedTimestamp = true) {
            it.testRunRepository.saveTestRun(publicId, parse(resultsXmlLoader.passing(), resultsXmlLoader.failingAnother()))
        }

        // Created timestamps differ since each backend saves at its own current time
        expectSameResult(ignoringCreatedTimestamp = true) { it.testRunRepository.fetchTestRunSummary(publicId) }
        expectSameResult { it.testSuiteRepository.fetchTestSuitesWithCases(publicId).map { suite -> suite.copy(testCases = null) } }
        expectSameResult { it.testRunMetadataRepository.fetchGitMetadata(publicId) }
        expectSameResult { it.testRunMetadataRepository.fetchResultsMetadata(publicId) }
    }

    @Test
    fun `appended test suites and updated summary should match`() {
        val publicId = randomPublicId()

        val initialResults =
            GroupedResults(
                listOf(GroupedTestSuites(parse(resultsXmlLoader.passing()), "Group1", "Label1", null)),
                listOf(),
                ResultsMetadata(null, true, "B12", Instant.parse("2026-10-17T10:15:30Z")),
                BigDecimal("10.0"),
                null,
                null,
            )
        expectSameResult { it.testRunRepository.saveGroupedTestRun(publicId, initialResults) }

        val appendedSuites =
            listOf(
                GroupedTestSuites(parse(resultsXmlLoader.failing()), "Group1", "Label changed", null),
                GroupedTestSuites(parse(resultsXmlLoader.output()), "Group2", "Label2", null),
            )

        expectSameResult { backend ->
            val startingIdx = backend.testSuiteRepository.fetchHighestTestSuiteIndex(publicId)!! + 1
            backend.testRunRepository.appendTestSuites(publicId, startingIdx, appendedSuites)
            val allTestSuites = backend.testSuiteRepository.fetchTestSuitesWithCases(publicId)
            backend.testRunRepository.updateTestRunSummary(publicId, allTestSuites, BigDecimal("5.5"))
                .copy(createdTimestamp = Instant.EPOCH)
        }

        expectSameTestRunData(publicId)
    }

    @Test
    fun `missing test run should match`() {
        expectSameTestRunData(randomPublicId())
    }

    @Test
    fun `system attributes should match`() {
        val publicId = randomPublicId()
        expectSameResult { it.testRunRepository.saveTestRun(publicId, parse(resultsXmlLoader.passing())).id }

        expectSameResult { it.testRunSystemAttributesRepository.fetchAttributes(publicId) }
        expectSameResult { it.testRunSystemAttributesRepository.pin(publicId) }
        expectSameResult { it.testRunSystemAttributesRepository.fetchAttributes(publicId) }
        expectSameResult { it.testRunSystemAttributesRepository.unpin(publicId) }
        expectSameResult { it.testRunSystemAttributesRepository.fetchAttributes(publicId) }

        val missingPublicId = randomPublicId()
        expectSameResult { it.testRunSystemAttributesRepository.pin(missingPublicId) }
        expectSameResult { it.testRunSystemAttributesRepository.fetchAttributes(missingPublicId) }
    }

    @Test
    fun `attachments should match`() {
        val publicId = randomPublicId()

        expectSameResult { it.attachmentRepository.listAttachments(publicId) }

        expectSameResult { it.attachmentRepository.addAttachment(publicId, Attachment("screenshot.png", "object-1", 1234L)) }
        expectSameResult { it.attachmentRepository.addAttachment(publicId, Attachment("video.mp4", "object-2", null)) }
        expectSameResult { it.attachmentRepository.listAttachments(publicId) }

        expectSameResult { it.attachmentRepository.deleteAttachment(publicId, "object-1") }
        expectSameResult { it.attachmentRepository.listAttachments(publicId) }
    }

    @Test
    fun `results processing should match`() {
        val publicId = randomPublicId()

        expectSameResult { it.resultsProcessingRepository.fetchResultsProcessing(publicId) }
        expectSameResult { it.resultsProcessingRepository.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.SUCCESS) }

        expectSameResult { it.resultsProcessingRepository.createResultsProcessing(publicId).copy(createdTimestamp = null) }
        expectSameResult { it.resultsProcessingRepository.fetchResultsProcessing(publicId)?.copy(createdTimestamp = null) }

        expectSameResult { it.resultsProcessingRepository.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.PROCESSING) }
        expectSameResult { it.resultsProcessingRepository.fetchResultsProcessing(publicId)?.copy(createdTimestamp = null) }

        expectSameResult { it.resultsProcessingRepository.recordResultsProcessingError(publicId, "<bad/>", "Bad results") }
        expectSameResult { it.resultsProcessingRepository.fetchResultsProcessing(publicId)?.copy(createdTimestamp = null) }
    }

    @Test
    fun `performance results and code quality reports should match`() {
        val publicId = randomPublicId()
        expectSameResult { it.testRunRepository.saveTestRun(publicId, parse(resultsXmlLoader.passing())).id }

        expectSameResult { it.performanceResultsRepository.fetchResults(publicId) }
        listOf(
            IncomingPerformanceResult("first", BigDecimal("12.34567"), 100, BigDecimal("1.5"), BigDecimal("3"), BigDecimal("2.0005")),
            IncomingPerformanceResult("second", BigDecimal("1"), 5, BigDecimal("0.1"), BigDecimal("0.2"), BigDecimal("0.15")),
        ).forEach { result ->
            expectSameResult { it.performanceResultsRepository.savePerformanceResults(publicId, result) }
        }
        expectSameResult { it.performanceResultsRepository.fetchResults(publicId) }

        expectSameResult { it.codeQualityReportRepository.fetchCodeQualityReports(publicId) }
        expectSameResult {
            it.codeQualityReportRepository.insertCodeQualityReports(
                publicId,
                listOf(CodeQualityReport(1, "lint output", "lint.txt", "group1"), CodeQualityReport(2, "more output", "other.txt", null)),
            )
        }
        expectSameResult { it.codeQualityReportRepository.fetchCodeQualityReports(publicId) }
    }

    @Test
    fun `coverage should match`() {
        val publicId = randomPublicId()
        expectSameResult { it.testRunRepository.saveTestRun(publicId, parse(resultsXmlLoader.passing())).id }

        expectSameCoverage(publicId, listOf("server", "ui"))

        expectSameResult { it.coverageRepository.createOrGetCoverageRun(publicId) }
        expectSameCoverage(publicId, listOf("server", "ui"))

        val serverReport = coverageReport("server", 75, 25, 60, 10, 33, 66)
        val serverFiles =
            listOf(
                coverageFile("File1.kt", "path/File1.kt", listOf(12, 13), listOf(14)),
                coverageFile("File2.kt", "path/File2.kt", listOf(20), listOf()),
                coverageFile("NoPath.kt", null, listOf(1), listOf(2)),
            )
        expectSameResult { it.coverageRepository.addCoverageReport(publicId, serverReport, serverFiles) }
        expectSameCoverage(publicId, listOf("server", "ui"))

        // Appending to the existing group, which updates the matching files and adds the new ones
        expectSameResult { it.coverageRepository.upsertCoverageGroup(publicId, serverReport, CoverageStat(50, 5)) }
        expectSameResult {
            it.coverageRepository.upsertCoverageFiles(
                publicId,
                "server",
                listOf(
                    coverageFile("File1.kt", "path/File1.kt", listOf(13), listOf(15)),
                    coverageFile("File3.kt", "path/File3.kt", listOf(30), listOf(31)),
                    coverageFile("NoPath.kt", null, listOf(99), listOf()),
                ),
            )
        }
        expectSameCoverage(publicId, listOf("server", "ui"))

        // Appending a new group
        val uiReport = coverageReport("ui", 10, 0, 20, 5, 1, 1)
        val uiGroupStatus = runBlocking { objectStore.coverageRepository.upsertCoverageGroup(publicId, uiReport, CoverageStat(1, 1)) }
        val databaseUiGroupStatus = runBlocking { database.coverageRepository.upsertCoverageGroup(publicId, uiReport, CoverageStat(1, 1)) }
        expectThat(uiGroupStatus).isEqualTo(databaseUiGroupStatus).isEqualTo(CoverageGroupStatus.NEW)
        expectSameResult {
            it.coverageRepository.insertCoverageFiles(publicId, "ui", listOf(coverageFile("App.tsx", "src/App.tsx", listOf(), listOf(3))))
        }
        expectSameCoverage(publicId, listOf("server", "ui"))

        expectSameResult { it.coverageRepository.deleteCoverage(publicId) }
        expectSameCoverage(publicId, listOf("server", "ui"))
        expectSameResult { it.coverageRepository.deleteCoverage(publicId) }
    }

    private fun expectSameTestRunData(publicId: PublicId) {
        // The database repository returns the test run's internal row ID as the summary ID, where the object store
        // returns the public ID, so compare the summary separately via fetchTestRunSummary
        expectSameResult {
            it.testRunRepository.fetchTestRun(publicId)?.let {
                    testRun ->
                testRun.copy(summary = testRun.summary.copy(id = ""))
            }
        }
        expectSameResult { it.testRunRepository.fetchTestRunSummary(publicId) }
        val objectStoreTestRun = runBlocking { objectStore.testRunRepository.fetchTestRun(publicId) }
        objectStoreTestRun?.let { expectThat(it.summary.id).isEqualTo(publicId.id) }

        expectSameResult { it.testSuiteRepository.fetchTestSuitesWithCases(publicId) }
        expectSameResult { it.testSuiteRepository.fetchTestSuites(publicId, TestSuiteSearchCriteria(null, false)) }
        expectSameResult { it.testSuiteRepository.fetchTestSuites(publicId, TestSuiteSearchCriteria(null, true)) }
        expectSameResult { it.testSuiteRepository.fetchTestSuites(publicId, TestSuiteSearchCriteria("projektor.example.spock", false)) }
        expectSameResult { it.testSuiteRepository.fetchHighestTestSuiteIndex(publicId) }

        expectSameResult { it.testCaseRepository.fetchTestCases(publicId) }
        expectSameResult { it.testCaseRepository.fetchFailedTestCases(publicId) }
        expectSameResult { it.testCaseRepository.fetchSlowTestCases(publicId, 3).map { testCase -> testCase.duration } }

        expectSameResult { it.testRunMetadataRepository.fetchGitMetadata(publicId) }
        expectSameResult { it.testRunMetadataRepository.fetchResultsMetadata(publicId) }

        val testSuites = runBlocking { database.testSuiteRepository.fetchTestSuitesWithCases(publicId) }

        (testSuites.map { it.idx } + listOf(0, 999)).forEach { testSuiteIdx ->
            expectSameResult { it.testSuiteRepository.fetchTestSuite(publicId, testSuiteIdx) }
            expectSameResult { it.testSuiteRepository.fetchTestSuiteSystemOut(publicId, testSuiteIdx) }
            expectSameResult { it.testSuiteRepository.fetchTestSuiteSystemErr(publicId, testSuiteIdx) }
        }

        testSuites.forEach { testSuite ->
            (testSuite.testCases.orEmpty().map { it.idx } + listOf(999)).forEach { testCaseIdx ->
                expectSameResult { it.testCaseRepository.fetchTestCase(publicId, testSuite.idx, testCaseIdx) }
                expectSameResult { it.testCaseRepository.fetchTestCaseSystemOut(publicId, testSuite.idx, testCaseIdx) }
                expectSameResult { it.testCaseRepository.fetchTestCaseSystemErr(publicId, testSuite.idx, testCaseIdx) }
            }
        }
    }

    private fun expectSameCoverage(
        publicId: PublicId,
        groupNames: List<String>,
    ) {
        expectSameResult { it.coverageRepository.coverageGroupExists(publicId) }
        expectSameResult { it.coverageRepository.fetchCoverageList(publicId) }
        // The database sums no rows into nulls, so only compare overall stats when there's coverage
        if (runBlocking { database.coverageRepository.coverageGroupExists(publicId) }) {
            expectSameResult { it.coverageRepository.fetchOverallStats(publicId) }
        }
        groupNames.forEach { groupName ->
            // CoverageFile holds arrays, which data class equality compares by reference
            expectSameResult { backend ->
                backend.coverageRepository.fetchCoverageFiles(publicId, groupName)
                    .map { listOf(it.fileName, it.directoryName, it.filePath, it.missedLines.toList(), it.partialLines.toList(), it.stats) }
                    .sortedBy { it.toString() }
            }
        }
    }

    private fun <T> expectSameResult(
        ignoringCreatedTimestamp: Boolean = false,
        fetch: suspend (Repositories) -> T,
    ) {
        val databaseResult = runBlocking { fetch(database) }
        val objectStoreResult = runBlocking { fetch(objectStore) }

        if (ignoringCreatedTimestamp && databaseResult is projektor.server.api.TestRunSummary && objectStoreResult is projektor.server.api.TestRunSummary) {
            expectThat(
                objectStoreResult.copy(createdTimestamp = Instant.EPOCH),
            ).isEqualTo(databaseResult.copy(createdTimestamp = Instant.EPOCH))
        } else {
            expectThat(objectStoreResult).isEqualTo(databaseResult)
        }
    }

    // Drops test suites without test cases, like GroupedResultsConverter does before results are saved
    private fun parse(vararg resultsXml: String) =
        resultsXml
            .flatMap { testResultsProcessor.parseResultsBlob(it) }
            .filter { !it.testCases.isNullOrEmpty() }
            .also { expectThat(it.size).isGreaterThan(0) }

    private fun coverageReport(
        name: String,
        statementCovered: Int,
        statementMissed: Int,
        lineCovered: Int,
        lineMissed: Int,
        branchCovered: Int,
        branchMissed: Int,
    ) = CoverageReport(
        name,
        CoverageReportStats(
            statementStat = CoverageReportStat(statementCovered, statementMissed),
            lineStat = CoverageReportStat(lineCovered, lineMissed),
            branchStat = CoverageReportStat(branchCovered, branchMissed),
        ),
        null,
    )

    private fun coverageFile(
        fileName: String,
        filePath: String?,
        missedLines: List<Int>,
        partialLines: List<Int>,
    ) = CoverageFile(
        fileName = fileName,
        directoryName = "dir",
        missedLines = missedLines.toTypedArray(),
        partialLines = partialLines.toTypedArray(),
        stats =
            CoverageStats(
                statementStat = CoverageStat(10, missedLines.size),
                lineStat = CoverageStat(20, missedLines.size),
                branchStat = CoverageStat(3, partialLines.size),
            ),
        filePath = filePath,
    )

    private class Repositories(koin: Koin) {
        val testRunRepository: TestRunRepository = koin.get()
        val testSuiteRepository: TestSuiteRepository = koin.get()
        val testCaseRepository: TestCaseRepository = koin.get()
        val testRunMetadataRepository: TestRunMetadataRepository = koin.get()
        val testRunSystemAttributesRepository: TestRunSystemAttributesRepository = koin.get()
        val attachmentRepository: AttachmentRepository = koin.get()
        val resultsProcessingRepository: ResultsProcessingRepository = koin.get()
        val performanceResultsRepository: PerformanceResultsRepository = koin.get()
        val codeQualityReportRepository: CodeQualityReportRepository = koin.get()
        val coverageRepository: CoverageRepository = koin.get()
    }

    companion object {
        private const val BUCKET_NAME = "persistenceparity"
    }
}
