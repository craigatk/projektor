package projektor.compare

import projektor.server.api.PublicId

interface PreviousTestRunRepository {
    suspend fun findPreviousMainBranchRunWithCoverage(publicId: PublicId): PublicId?

    suspend fun findMostRecentMainBranchRunWithCoverage(repoName: String, projectName: String?): PublicId?
}
