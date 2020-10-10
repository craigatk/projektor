package projektor.error

import projektor.server.api.PublicId
import java.lang.Exception

class ProcessingFailureService(private val processingFailureRepository: ProcessingFailureRepository) {

    suspend fun recordProcessingFailure(publicId: PublicId, body: String, bodyType: FailureBodyType, e: Exception) {
        processingFailureRepository.recordProcessingFailure(
            publicId = publicId,
            body = body,
            bodyType = bodyType,
            failure = e.message
        )
    }
}
