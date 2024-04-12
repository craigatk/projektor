package projektor.metadata

import projektor.server.api.PublicId
import projektor.server.api.metadata.TestRunGitMetadata
import projektor.server.api.metadata.TestRunMetadata

class TestRunMetadataService(private val testRunMetadataRepository: TestRunMetadataRepository) {
    suspend fun fetchGitMetadata(publicId: PublicId): TestRunGitMetadata? = testRunMetadataRepository.fetchGitMetadata(publicId)

    suspend fun fetchResultsMetadata(publicId: PublicId): TestRunMetadata? = testRunMetadataRepository.fetchResultsMetadata(publicId)
}
