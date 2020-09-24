package projektor.metadata

import projektor.server.api.PublicId
import projektor.server.api.metadata.TestRunGitMetadata
import projektor.server.api.metadata.TestRunMetadata

interface TestRunMetadataRepository {
    suspend fun fetchGitMetadata(publicId: PublicId): TestRunGitMetadata?

    suspend fun fetchResultsMetadata(publicId: PublicId): TestRunMetadata?
}
