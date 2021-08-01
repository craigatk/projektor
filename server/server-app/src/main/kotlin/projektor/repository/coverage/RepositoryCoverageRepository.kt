package projektor.repository.coverage

import projektor.server.api.repository.coverage.RepositoryCoverageTimeline

interface RepositoryCoverageRepository {
    suspend fun fetchRepositoryCoverageTimeline(repoName: String, projectName: String?): RepositoryCoverageTimeline?
}
