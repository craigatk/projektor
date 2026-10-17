package projektor.testrun

import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.parser.model.TestSuite
import projektor.server.api.PublicId
import projektor.server.api.TestRun
import projektor.server.api.TestRunSummary
import java.math.BigDecimal
import java.time.LocalDate
import projektor.server.api.TestSuite as TestSuiteApi

interface TestRunRepository {
    suspend fun saveTestRun(
        publicId: PublicId,
        testSuites: List<TestSuite>,
    ): TestRunSummary

    suspend fun saveGroupedTestRun(
        publicId: PublicId,
        groupedResults: GroupedResults,
    ): TestRunSummary

    suspend fun appendTestSuites(
        publicId: PublicId,
        startingIdx: Int,
        groupedTestSuites: List<GroupedTestSuites>,
    )

    suspend fun updateTestRunSummary(
        publicId: PublicId,
        testSuites: List<TestSuiteApi>,
        wallClockDuration: BigDecimal?,
    ): TestRunSummary

    suspend fun fetchTestRun(publicId: PublicId): TestRun?

    suspend fun fetchTestRunSummary(publicId: PublicId): TestRunSummary?

    /**
     * Deletes the test run and the data owned by it: test suites, test cases, failures, suite groups,
     * results and git metadata, system attributes, performance results, and code quality reports.
     * Coverage, attachments and results processing status are NOT deleted here; callers remove those
     * through their own repositories (see TestRunCleanupService).
     */
    suspend fun deleteTestRun(publicId: PublicId)

    suspend fun findTestRunsCreatedBeforeAndNotPinned(createdBefore: LocalDate): List<PublicId>

    suspend fun findTestRunsCreatedBeforeAndNotPinnedWithAttachments(createdBefore: LocalDate): List<PublicId>

    suspend fun findTestRunWithGroup(
        group: String,
        repoName: String,
    ): PublicId?
}
