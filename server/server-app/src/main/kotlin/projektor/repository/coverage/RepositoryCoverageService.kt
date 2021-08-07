package projektor.repository.coverage

import projektor.server.api.repository.BranchType
import projektor.server.api.repository.coverage.RepositoryCoverageTimeline

class RepositoryCoverageService(private val repositoryCoverageRepository: RepositoryCoverageRepository) {
    suspend fun fetchRepositoryCoverageTimeline(branchType: BranchType, repoName: String, projectName: String?): RepositoryCoverageTimeline? =
        repositoryCoverageRepository.fetchRepositoryCoverageTimeline(branchType, repoName, projectName)
}
