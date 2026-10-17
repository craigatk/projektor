package projektor.incomingresults.processing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import projektor.error.ProcessingFailureRepository
import projektor.objectstore.json.JsonObjectStore
import projektor.persistence.objectstore.ObjectStoreKeys
import projektor.persistence.objectstore.ResultsProcessingDocument
import projektor.persistence.objectstore.toStoredPrecision
import projektor.server.api.PublicId
import projektor.server.api.error.FailureBodyType
import projektor.server.api.results.ResultsProcessing
import projektor.server.api.results.ResultsProcessingStatus
import java.time.LocalDateTime

class ResultsProcessingObjectStoreRepository(
    private val store: JsonObjectStore,
    private val processingFailureRepository: ProcessingFailureRepository,
) : ResultsProcessingRepository {
    override suspend fun createResultsProcessing(publicId: PublicId): ResultsProcessing =
        withContext(Dispatchers.IO) {
            val createdTimestamp = LocalDateTime.now()

            store.create(
                ObjectStoreKeys.resultsProcessing(publicId),
                ResultsProcessingDocument(ResultsProcessingStatus.RECEIVED.name, createdTimestamp.toStoredPrecision(), null),
            ) ?: throw IllegalStateException("Results processing for $publicId already exists")

            ResultsProcessing(publicId.id, ResultsProcessingStatus.RECEIVED, createdTimestamp, null)
        }

    override suspend fun updateResultsProcessingStatus(
        publicId: PublicId,
        newStatus: ResultsProcessingStatus,
    ): Boolean = updateResultsProcessing(publicId) { it.copy(status = newStatus.name) }

    override suspend fun recordResultsProcessingError(
        publicId: PublicId,
        resultsBody: String,
        errorMessage: String?,
    ): Boolean {
        val updatedProcessingStatus =
            updateResultsProcessing(publicId) { it.copy(status = ResultsProcessingStatus.ERROR.name, errorMessage = errorMessage) }

        processingFailureRepository.recordProcessingFailure(
            publicId = publicId,
            body = resultsBody,
            bodyType = FailureBodyType.TEST_RESULTS,
            failureMessage = errorMessage,
        )

        return updatedProcessingStatus
    }

    override suspend fun fetchResultsProcessing(publicId: PublicId): ResultsProcessing? =
        withContext(Dispatchers.IO) {
            store.read<ResultsProcessingDocument>(ObjectStoreKeys.resultsProcessing(publicId))?.let {
                ResultsProcessing(publicId.id, ResultsProcessingStatus.valueOf(it.status), it.createdTimestamp, it.errorMessage)
            }
        }

    // Returns whether there was results processing to update, like the database repository's updated row count
    private suspend fun updateResultsProcessing(
        publicId: PublicId,
        transform: (ResultsProcessingDocument) -> ResultsProcessingDocument,
    ): Boolean =
        withContext(Dispatchers.IO) {
            store.update<ResultsProcessingDocument>(ObjectStoreKeys.resultsProcessing(publicId)) { current ->
                current?.let(transform)
            } != null
        }
}
