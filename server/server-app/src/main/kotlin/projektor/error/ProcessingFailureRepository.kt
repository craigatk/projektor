package projektor.error

import projektor.server.api.PublicId

interface ProcessingFailureRepository {
    suspend fun recordProcessingFailure(publicId: PublicId, body: String, bodyType: FailureBodyType, failure: String?)
}
