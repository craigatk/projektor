package projektor.metadata

import projektor.server.api.PublicId
import projektor.server.api.metadata.TestRunGitMetadata

interface TestRunMetadataRepository {
    suspend fun fetchGitMetadata(publicId: PublicId): TestRunGitMetadata?
}
