package projektor.error

import projektor.server.api.PublicId
import projektor.server.api.error.FailureBodyType
import projektor.server.api.error.ResultsProcessingFailure

class ProcessingFailureService(private val processingFailureRepository: ProcessingFailureRepository) {
    suspend fun recordProcessingFailure(
        publicId: PublicId,
        body: String,
        bodyType: FailureBodyType,
        e: Exception,
    ) {
        processingFailureRepository.recordProcessingFailure(
            publicId = publicId,
            body = body,
            bodyType = bodyType,
            failureMessage = e.message,
        )
    }

    suspend fun fetchRecentProcessingFailures(failureCount: Int): List<ResultsProcessingFailure> =
        processingFailureRepository.fetchRecentProcessingFailures(failureCount.coerceIn(1, MAX_RECENT_FAILURES))

    companion object {
        // Each failure holds a whole upload's body, so this bounds the memory one request can use
        const val MAX_RECENT_FAILURES = 100
    }
}
