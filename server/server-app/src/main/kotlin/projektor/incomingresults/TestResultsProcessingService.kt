package projektor.incomingresults

import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.server.api.PublicId
import projektor.server.api.results.ResultsProcessing
import projektor.server.api.results.ResultsProcessingStatus

class TestResultsProcessingService(
    private val resultsProcessingRepository: ResultsProcessingRepository
) {
    suspend fun createResultsProcessing(publicId: PublicId) =
            resultsProcessingRepository.createResultsProcessing(publicId)

    suspend fun fetchResultsProcessing(publicId: PublicId): ResultsProcessing? =
            resultsProcessingRepository.fetchResultsProcessing(publicId)

    suspend fun updateResultsProcessingStatus(publicId: PublicId, newStatus: ResultsProcessingStatus) =
            resultsProcessingRepository.updateResultsProcessingStatus(publicId, newStatus)

    suspend fun recordResultsProcessingError(publicId: PublicId, resultsBody: String, errorMessage: String?) =
            resultsProcessingRepository.recordResultsProcessingError(publicId, resultsBody, errorMessage)
}
