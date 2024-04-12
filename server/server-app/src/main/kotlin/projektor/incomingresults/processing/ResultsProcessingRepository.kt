package projektor.incomingresults.processing

import projektor.server.api.PublicId
import projektor.server.api.results.ResultsProcessing
import projektor.server.api.results.ResultsProcessingStatus

interface ResultsProcessingRepository {
    suspend fun createResultsProcessing(publicId: PublicId): ResultsProcessing

    suspend fun updateResultsProcessingStatus(
        publicId: PublicId,
        newStatus: ResultsProcessingStatus,
    ): Boolean

    suspend fun recordResultsProcessingError(
        publicId: PublicId,
        resultsBody: String,
        errorMessage: String?,
    ): Boolean

    suspend fun fetchResultsProcessing(publicId: PublicId): ResultsProcessing?
}
