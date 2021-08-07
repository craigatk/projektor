package projektor.compare

import projektor.server.api.PublicId
import projektor.server.api.repository.BranchType

interface PreviousTestRunRepository {
    suspend fun findPreviousMainBranchRunWithCoverage(publicId: PublicId): PublicId?

    suspend fun findMostRecentRunWithCoverage(branchType: BranchType, repoName: String, projectName: String?): RecentTestRun?
}
