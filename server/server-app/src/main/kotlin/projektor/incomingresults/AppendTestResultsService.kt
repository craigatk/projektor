package projektor.incomingresults

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import projektor.incomingresults.model.GroupedResults
import projektor.server.api.PublicId
import projektor.server.api.TestRunSummary
import projektor.testrun.TestRunRepository
import projektor.testsuite.TestSuiteRepository

class AppendTestResultsService(
    private val testRunRepository: TestRunRepository,
    private val testSuiteRepository: TestSuiteRepository,
) {
    suspend fun findExistingTestRunWithGroup(
        group: String?,
        repoName: String?,
    ): PublicId? =
        if (group != null && repoName != null) {
            testRunRepository.findTestRunWithGroup(group, repoName)
        } else {
            null
        }

    suspend fun appendGroupedTestRun(
        existingPublicId: PublicId,
        groupedResults: GroupedResults,
    ): Pair<Long, TestRunSummary> =
        withContext(Dispatchers.IO) {
            // Find the highest test suite index for the existing public ID
            val highestExistingTestSuiteIdx = testSuiteRepository.fetchHighestTestSuiteIndex(existingPublicId) ?: 0

            // Save the test suites, starting with that highest index
            val testRunId =
                testRunRepository.appendTestSuites(
                    existingPublicId,
                    highestExistingTestSuiteIdx + 1,
                    groupedResults.groupedTestSuites,
                )

            // Fetch back all the test suites
            val allTestSuites = testSuiteRepository.fetchTestSuitesWithCases(existingPublicId)

            // Calculate the new top-level test run data using all the test suites and update the values
            val testRunSummary = testRunRepository.updateTestRunSummary(testRunId, allTestSuites, groupedResults.wallClockDuration)

            Pair(testRunId, testRunSummary)
        }
}
