package projektor.testrun

import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.parser.model.TestSuite
import projektor.server.api.PublicId
import projektor.server.api.TestRun
import projektor.server.api.TestRunSummary
import java.math.BigDecimal
import java.time.LocalDate

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

    /**
     * Recalculates the test run summary from all of its saved test suites, adding the wall clock duration
     * to the existing one. Atomic with respect to concurrent appends to the same test run.
     */
    suspend fun updateTestRunSummary(
        publicId: PublicId,
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

    /**
     * Claims the results group in the repo for the test run, returning the public ID of the test run that owns
     * the group: the given test run if no test run had claimed the group yet, otherwise the existing owner.
     * Atomic, so concurrent uploads with the same group agree on one test run even before it's saved.
     */
    suspend fun claimTestRunGroup(
        group: String,
        repoName: String,
        publicId: PublicId,
    ): PublicId

    /**
     * Releases the test run's claim on the results group, if it still has it, so the next upload with the
     * group starts a new test run.
     */
    suspend fun releaseTestRunGroupClaim(
        group: String,
        repoName: String,
        publicId: PublicId,
    )

    suspend fun findTestRunWithGroup(
        group: String,
        repoName: String,
    ): PublicId?
}
