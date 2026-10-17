package projektor.persistence.objectstore

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.incomingresults.model.GitMetadata
import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.incomingresults.model.ResultsMetadata
import projektor.incomingresults.randomPublicId
import projektor.server.api.PublicId
import projektor.server.api.attachments.Attachment
import projektor.server.api.quality.CodeQualityReport
import projektor.server.api.results.ResultsProcessingStatus
import projektor.testsuite.TestSuiteSearchCriteria
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import java.math.BigDecimal
import java.time.Instant
import projektor.incomingresults.model.PerformanceResult as IncomingPerformanceResult

/**
 * Checks that the object-store backend returns the same data for a single test run as the database backend.
 */
class ObjectStoreParityTest : ObjectStoreParityTestCase() {
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
                                // Output on the test case rather than the test suite, with empty test suite output
                                resultsXmlLoader.gradleSingleTestCaseSystemOutFail(),
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

        val testCaseWithOwnOutput = testCases.first { it.hasSystemOutTestCase && it.hasSystemErrTestCase }
        val testCaseSystemOut =
            runBlocking {
                objectStore.testCaseRepository.fetchTestCaseSystemOut(
                    publicId,
                    testCaseWithOwnOutput.testSuiteIdx,
                    testCaseWithOwnOutput.idx,
                )
            }
        expectThat(testCaseSystemOut.value.orEmpty()).contains("LoggerContext")

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
            backend.testRunRepository.updateTestRunSummary(publicId, BigDecimal("5.5"))
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
        expectSameResult {
            it.coverageRepository.appendCoverageReport(
                publicId,
                serverReport,
                // Not including a file without a path, which the database counts in the group's line stats on every
                // append without saving it, where the object store calculates the group's stats from its saved files
                listOf(
                    coverageFile("File1.kt", "path/File1.kt", listOf(13), listOf(15)),
                    coverageFile("File3.kt", "path/File3.kt", listOf(30), listOf(31)),
                ),
            )
        }
        expectSameCoverage(publicId, listOf("server", "ui"))

        // Appending a new group, then appending to it again
        val uiReport = coverageReport("ui", 10, 0, 20, 5, 1, 1)
        expectSameResult {
            it.coverageRepository.appendCoverageReport(
                publicId,
                uiReport,
                listOf(coverageFile("App.tsx", "src/App.tsx", listOf(1, 2), listOf(3))),
            )
        }
        expectSameCoverage(publicId, listOf("server", "ui"))
        expectSameResult {
            it.coverageRepository.appendCoverageReport(
                publicId,
                uiReport,
                listOf(coverageFile("App.tsx", "src/App.tsx", listOf(2), listOf())),
            )
        }
        expectSameCoverage(publicId, listOf("server", "ui"))

        expectSameResult { it.coverageRepository.deleteCoverage(publicId) }
        expectSameCoverage(publicId, listOf("server", "ui"))
        expectSameResult { it.coverageRepository.deleteCoverage(publicId) }
    }

    private fun expectSameTestRunData(publicId: PublicId) {
        // The database repository returns the test run's internal row ID as the summary ID, where the object store
        // returns the public ID, so compare the summary separately via fetchTestRunSummary. The database's query also
        // doesn't order the test suites, so compare them in index order.
        expectSameResult { backend ->
            backend.testRunRepository.fetchTestRun(publicId)?.let { testRun ->
                testRun.copy(summary = testRun.summary.copy(id = ""), testSuites = testRun.testSuites?.sortedBy { it.idx })
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
}
