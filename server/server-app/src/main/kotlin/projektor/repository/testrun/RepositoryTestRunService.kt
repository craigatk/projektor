package projektor.repository.testrun

import projektor.server.api.repository.RepositoryFlakyTest

class RepositoryTestRunService(private val repositoryTestRunRepository: RepositoryTestRunRepository) {
    private val flakyTestCalculator = FlakyTestCalculator()

    suspend fun fetchRepositoryTestRunTimeline(repoName: String, projectName: String?) =
        repositoryTestRunRepository.fetchRepositoryTestRunTimeline(repoName, projectName)

    suspend fun fetchFlakyTests(repoName: String, projectName: String?, maxRuns: Int, flakyFailureThreshold: Int): List<RepositoryFlakyTest> {
        val failingTestCases = repositoryTestRunRepository.fetchRepositoryFailingTestCases(repoName, projectName, maxRuns)

        return flakyTestCalculator.calculateFlakyTests(failingTestCases, flakyFailureThreshold)
    }
}
