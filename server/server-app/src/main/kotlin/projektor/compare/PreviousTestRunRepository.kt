package projektor.compare

import projektor.server.api.PublicId

interface PreviousTestRunRepository {
    suspend fun findPreviousMainBranchRun(publicId: PublicId): PublicId?
}
