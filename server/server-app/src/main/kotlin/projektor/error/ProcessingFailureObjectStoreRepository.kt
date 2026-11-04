package projektor.error

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import projektor.objectstore.json.JsonObjectStore
import projektor.persistence.objectstore.ObjectStoreKeys
import projektor.persistence.objectstore.ProcessingFailureDocument
import projektor.persistence.objectstore.mapParallel
import projektor.persistence.objectstore.toStoredPrecision
import projektor.server.api.PublicId
import projektor.server.api.error.FailureBodyType
import projektor.server.api.error.ResultsProcessingFailure
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class ProcessingFailureObjectStoreRepository(private val store: JsonObjectStore) : ProcessingFailureRepository {
    override suspend fun recordProcessingFailure(
        publicId: PublicId,
        body: String,
        bodyType: FailureBodyType,
        failureMessage: String?,
    ): ResultsProcessingFailure =
        withContext(Dispatchers.IO) {
            val createdTimestamp = LocalDateTime.now().toStoredPrecision()

            store.write(
                ObjectStoreKeys.processingFailure(createdTimestamp.toInstant(ZoneOffset.UTC), publicId, UUID.randomUUID().toString()),
                ProcessingFailureDocument(publicId.id, body, bodyType.name, createdTimestamp, failureMessage),
            )

            ResultsProcessingFailure(
                id = publicId.id,
                body = body,
                bodyType = bodyType,
                failureMessage = failureMessage,
                createdTimestamp = createdTimestamp.toInstant(ZoneOffset.UTC),
            )
        }

    override suspend fun fetchRecentProcessingFailures(failureCount: Int): List<ResultsProcessingFailure> {
        // Keys sort newest first
        val failureKeys =
            withContext(Dispatchers.IO) {
                store.list(ObjectStoreKeys.PROCESSING_FAILURES_PREFIX, limit = failureCount).objectNames
            }

        return failureKeys
            .mapParallel { key -> store.read<ProcessingFailureDocument>(key) }
            .filterNotNull()
            .map {
                ResultsProcessingFailure(
                    id = it.publicId,
                    body = it.body,
                    bodyType = FailureBodyType.valueOf(it.bodyType),
                    createdTimestamp = it.createdTimestamp.toInstant(ZoneOffset.UTC),
                    failureMessage = it.failureMessage,
                )
            }
    }
}
