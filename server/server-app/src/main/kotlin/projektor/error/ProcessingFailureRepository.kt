package projektor.error

import projektor.server.api.PublicId
import projektor.server.api.error.FailureBodyType
import projektor.server.api.error.ResultsProcessingFailure

interface ProcessingFailureRepository {
    suspend fun recordProcessingFailure(
        publicId: PublicId,
        body: String,
        bodyType: FailureBodyType,
        failureMessage: String?,
    ): ResultsProcessingFailure

    suspend fun fetchRecentProcessingFailures(failureCount: Int): List<ResultsProcessingFailure>
}
