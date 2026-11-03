package projektor.repository.testrun

import projektor.server.api.TestRunSummary
import projektor.server.api.repository.BranchType
import projektor.server.api.repository.RepositoryFlakyTest
import kotlin.math.min

class RepositoryTestRunService(private val repositoryTestRunRepository: RepositoryTestRunRepository) {
    private val flakyTestCalculator = FlakyTestCalculator()

    suspend fun fetchRepositoryTestRunTimeline(
        repoName: String,
        projectName: String?,
    ) = repositoryTestRunRepository.fetchRepositoryTestRunTimeline(repoName, projectName)

    suspend fun fetchFlakyTests(
        repoName: String,
        projectName: String?,
        maxRuns: Int,
        flakyFailureThreshold: Int,
        branchType: BranchType,
    ): List<RepositoryFlakyTest> {
        val cappedMaxRuns = maxRuns.coerceIn(1, MAX_FLAKY_TEST_RUNS)

        val failingTestCases = repositoryTestRunRepository.fetchRepositoryFailingTestCases(repoName, projectName, cappedMaxRuns, branchType)
        val totalTestRunCount = repositoryTestRunRepository.fetchTestRunCount(repoName, projectName)
        val testRunCount = min(cappedMaxRuns.toLong(), totalTestRunCount)

        return flakyTestCalculator.calculateFlakyTests(failingTestCases, flakyFailureThreshold, testRunCount)
    }

    suspend fun fetchRepositoryTestRunSummaries(
        repoName: String,
        projectName: String?,
        limit: Int,
    ): List<TestRunSummary> = repositoryTestRunRepository.fetchRepositoryTestRunSummaries(repoName, projectName, limit)

    companion object {
        // The most test runs a flaky test request can look at, since its unauthenticated callers choose how many
        const val MAX_FLAKY_TEST_RUNS = 500
    }
}
