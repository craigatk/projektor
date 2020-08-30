package projektor.compare

import projektor.server.api.PublicId

class PreviousTestRunService(private val previousTestRunRepository: PreviousTestRunRepository) {
    suspend fun findPreviousMainBranchRun(publicId: PublicId): PublicId? =
            previousTestRunRepository.findPreviousMainBranchRun(publicId)
}
