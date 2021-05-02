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
    suspend fun saveTestRun(publicId: PublicId, testSuites: List<TestSuite>): TestRunSummary

    suspend fun saveGroupedTestRun(publicId: PublicId, groupedResults: GroupedResults): Pair<Long, TestRunSummary>

    suspend fun appendTestSuites(publicId: PublicId, startingIdx: Int, groupedTestSuites: List<GroupedTestSuites>): Long

    suspend fun updateTestRunSummary(testRunId: Long, testSuites: List<TestSuiteApi>, wallClockDuration: BigDecimal?): TestRunSummary

    suspend fun fetchTestRun(publicId: PublicId): TestRun?

    suspend fun fetchTestRunSummary(publicId: PublicId): TestRunSummary?

    suspend fun deleteTestRun(publicId: PublicId)

    suspend fun findTestRunsCreatedBeforeAndNotPinned(createdBefore: LocalDate): List<PublicId>

    suspend fun findTestRunsCreatedBeforeAndNotPinnedWithAttachments(createdBefore: LocalDate): List<PublicId>

    suspend fun findTestRunWithGroup(group: String, repoName: String): PublicId?
}
