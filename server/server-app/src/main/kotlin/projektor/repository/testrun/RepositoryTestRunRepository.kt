package projektor.repository.testrun

import projektor.server.api.PublicId
import projektor.server.api.TestCase
import projektor.server.api.repository.RepositoryTestRunTimeline

interface RepositoryTestRunRepository {
    suspend fun fetchRepositoryTestRunTimeline(repoName: String, projectName: String?): RepositoryTestRunTimeline?

    suspend fun fetchRepositoryFailingTestCases(repoName: String, projectName: String?, maxRuns: Int): List<TestCase>

    suspend fun fetchRecentTestRunPublicIds(repoName: String, projectName: String?, limit: Int): List<PublicId>
}
