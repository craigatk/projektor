package projektor.repository.coverage

import projektor.server.api.repository.BranchType
import projektor.server.api.repository.coverage.RepositoryCoverageTimeline

interface RepositoryCoverageRepository {
    suspend fun fetchRepositoryCoverageTimeline(branchType: BranchType, repoName: String, projectName: String?): RepositoryCoverageTimeline?

    suspend fun coverageExists(repoName: String, projectName: String?): Boolean
}
