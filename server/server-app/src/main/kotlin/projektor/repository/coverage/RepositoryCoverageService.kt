package projektor.repository.coverage

import projektor.server.api.repository.coverage.RepositoryCoverageTimeline

class RepositoryCoverageService(private val repositoryCoverageRepository: RepositoryCoverageRepository) {
    suspend fun fetchRepositoryCoverageTimeline(repoName: String, projectName: String?): RepositoryCoverageTimeline? =
        repositoryCoverageRepository.fetchRepositoryCoverageTimeline(repoName, projectName)
}
