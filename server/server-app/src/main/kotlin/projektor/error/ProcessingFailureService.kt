package projektor.error

import projektor.server.api.PublicId
import projektor.server.api.error.FailureBodyType
import projektor.server.api.error.ResultsProcessingFailure

class ProcessingFailureService(private val processingFailureRepository: ProcessingFailureRepository) {

    suspend fun recordProcessingFailure(publicId: PublicId, body: String, bodyType: FailureBodyType, e: Exception) {
        processingFailureRepository.recordProcessingFailure(
            publicId = publicId,
            body = body,
            bodyType = bodyType,
            failureMessage = e.message
        )
    }

    suspend fun fetchRecentProcessingFailures(failureCount: Int): List<ResultsProcessingFailure> =
        processingFailureRepository.fetchRecentProcessingFailures(failureCount)
}
