package projektor.compare

import projektor.server.api.PublicId

class PreviousTestRunService(private val previousTestRunRepository: PreviousTestRunRepository) {
    suspend fun findPreviousMainBranchRunWithCoverage(publicId: PublicId): PublicId? =
        previousTestRunRepository.findPreviousMainBranchRunWithCoverage(publicId)

    suspend fun findMostRecentMainBranchRunWithCoverage(repoName: String, projectName: String?): PublicId? =
        previousTestRunRepository.findMostRecentMainBranchRunWithCoverage(repoName, projectName)
}
