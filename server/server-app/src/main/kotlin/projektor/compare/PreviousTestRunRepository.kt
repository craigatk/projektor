package projektor.compare

import projektor.server.api.PublicId
import projektor.server.api.repository.BranchSearch

interface PreviousTestRunRepository {
    suspend fun findPreviousMainBranchRunWithCoverage(publicId: PublicId): PublicId?

    suspend fun findMostRecentRunWithCoverage(repoName: String, projectName: String?, branch: BranchSearch?): RecentTestRun?
}
