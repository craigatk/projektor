package projektor.compare

import projektor.server.api.PublicId
import projektor.server.api.repository.BranchSearch

class PreviousTestRunService(private val previousTestRunRepository: PreviousTestRunRepository) {
    suspend fun findPreviousMainBranchRunWithCoverage(publicId: PublicId): PublicId? =
        previousTestRunRepository.findPreviousMainBranchRunWithCoverage(publicId)

    suspend fun findMostRecentRunWithCoverage(repoName: String, projectName: String?, branch: BranchSearch?): RecentTestRun? =
        previousTestRunRepository.findMostRecentRunWithCoverage(repoName, projectName, branch)
}
