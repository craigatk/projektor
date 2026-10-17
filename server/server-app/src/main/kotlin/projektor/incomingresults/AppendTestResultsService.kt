package projektor.incomingresults

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.server.api.PublicId
import projektor.server.api.TestRunSummary
import projektor.server.api.results.ResultsProcessingStatus
import projektor.testrun.TestRunRepository
import projektor.testsuite.TestSuiteRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class AppendTestResultsService(
    private val testRunRepository: TestRunRepository,
    private val testSuiteRepository: TestSuiteRepository,
    private val resultsProcessingRepository: ResultsProcessingRepository,
    private val waitForTestRunTimeout: Duration = 60.seconds,
) {
    /**
     * Claims the results group for the test run, returning the public ID of the test run that owns the group
     * (the given one if it's the first with the group), or null if the results don't have a group.
     */
    suspend fun claimTestRunGroup(
        group: String?,
        repoName: String?,
        publicId: PublicId,
    ): PublicId? =
        if (group != null && repoName != null) {
            testRunRepository.claimTestRunGroup(group, repoName, publicId)
        } else {
            null
        }

    suspend fun appendGroupedTestRun(
        existingPublicId: PublicId,
        groupedResults: GroupedResults,
    ): TestRunSummary =
        withContext(Dispatchers.IO) {
            // The test run owning the group may still be saving its own results
            waitForTestRun(existingPublicId)

            // Find the highest test suite index for the existing public ID
            val highestExistingTestSuiteIdx = testSuiteRepository.fetchHighestTestSuiteIndex(existingPublicId) ?: 0

            // Save the test suites, starting with that highest index
            testRunRepository.appendTestSuites(
                existingPublicId,
                highestExistingTestSuiteIdx + 1,
                groupedResults.groupedTestSuites,
            )

            // Recalculate the top-level test run data from all the test suites, including ones appended concurrently
            testRunRepository.updateTestRunSummary(existingPublicId, groupedResults.wallClockDuration)
        }

    private suspend fun waitForTestRun(publicId: PublicId) {
        val deadline = TimeSource.Monotonic.markNow() + waitForTestRunTimeout

        while (testRunRepository.fetchTestRunSummary(publicId) == null) {
            if (resultsProcessingRepository.fetchResultsProcessing(publicId)?.status == ResultsProcessingStatus.ERROR) {
                throw IllegalStateException("Test run $publicId to append results to failed to save")
            }
            if (deadline.hasPassedNow()) {
                throw IllegalStateException("Test run $publicId to append results to was not saved within $waitForTestRunTimeout")
            }
            delay(250)
        }
    }
}
