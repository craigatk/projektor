package projektor.testrun.attributes

import projektor.server.api.PublicId

class TestRunSystemAttributesService(private val testRunSystemAttributesRepository: TestRunSystemAttributesRepository) {
    suspend fun fetchAttributes(publicId: PublicId) = testRunSystemAttributesRepository.fetchAttributes(publicId)

    suspend fun pin(publicId: PublicId) = testRunSystemAttributesRepository.pin(publicId)

    suspend fun unpin(publicId: PublicId) = testRunSystemAttributesRepository.unpin(publicId)
}
