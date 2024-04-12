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
        val failingTestCases = repositoryTestRunRepository.fetchRepositoryFailingTestCases(repoName, projectName, maxRuns, branchType)
        val totalTestRunCount = repositoryTestRunRepository.fetchTestRunCount(repoName, projectName)
        val testRunCount = min(maxRuns.toLong(), totalTestRunCount)

        return flakyTestCalculator.calculateFlakyTests(failingTestCases, flakyFailureThreshold, testRunCount)
    }

    suspend fun fetchRepositoryTestRunSummaries(
        repoName: String,
        projectName: String?,
        limit: Int,
    ): List<TestRunSummary> = repositoryTestRunRepository.fetchRepositoryTestRunSummaries(repoName, projectName, limit)
}
