package projektor.metadata

import projektor.server.api.PublicId
import projektor.server.api.metadata.TestRunGitMetadata

class TestRunMetadataService(private val testRunMetadataRepository: TestRunMetadataRepository) {
    suspend fun fetchGitMetadata(publicId: PublicId): TestRunGitMetadata? =
            testRunMetadataRepository.fetchGitMetadata(publicId)
}
