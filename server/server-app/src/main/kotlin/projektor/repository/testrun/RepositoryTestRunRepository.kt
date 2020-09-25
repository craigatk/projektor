package projektor.repository.testrun

import projektor.server.api.repository.RepositoryTestRunTimeline

interface RepositoryTestRunRepository {
    suspend fun fetchRepositoryTestRunTimeline(repoName: String, projectName: String?): RepositoryTestRunTimeline?
}
