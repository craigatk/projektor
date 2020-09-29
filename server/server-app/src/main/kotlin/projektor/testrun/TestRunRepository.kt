package projektor.testrun

import projektor.incomingresults.model.GroupedResults
import projektor.parser.model.TestSuite
import projektor.server.api.PublicId
import projektor.server.api.TestRun
import projektor.server.api.TestRunSummary
import java.time.LocalDate

interface TestRunRepository {
    suspend fun saveTestRun(publicId: PublicId, testSuites: List<TestSuite>): TestRunSummary

    suspend fun saveGroupedTestRun(publicId: PublicId, groupedResults: GroupedResults): TestRunSummary

    suspend fun fetchTestRun(publicId: PublicId): TestRun?

    suspend fun fetchTestRunSummary(publicId: PublicId): TestRunSummary?

    suspend fun deleteTestRun(publicId: PublicId)

    suspend fun findTestRunsToDelete(createdBefore: LocalDate): List<PublicId>
}
