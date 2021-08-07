package projektor.compare

import projektor.server.api.PublicId
import projektor.server.api.repository.BranchType

class PreviousTestRunService(private val previousTestRunRepository: PreviousTestRunRepository) {
    suspend fun findPreviousMainBranchRunWithCoverage(publicId: PublicId): PublicId? =
        previousTestRunRepository.findPreviousMainBranchRunWithCoverage(publicId)

    suspend fun findMostRecentMainBranchRunWithCoverage(branchType: BranchType, repoName: String, projectName: String?): RecentTestRun? =
        previousTestRunRepository.findMostRecentRunWithCoverage(branchType, repoName, projectName)
}
