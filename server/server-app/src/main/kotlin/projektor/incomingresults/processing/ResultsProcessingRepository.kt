package projektor.incomingresults.processing

import projektor.server.api.PublicId
import projektor.server.api.ResultsProcessing
import projektor.server.api.ResultsProcessingStatus

interface ResultsProcessingRepository {
    suspend fun createResultsProcessing(publicId: PublicId): ResultsProcessing

    suspend fun updateResultsProcessingStatus(publicId: PublicId, newStatus: ResultsProcessingStatus): Boolean

    suspend fun recordResultsProcessingError(publicId: PublicId, errorMessage: String?): Boolean

    suspend fun fetchResultsProcessing(publicId: PublicId): ResultsProcessing?
}
