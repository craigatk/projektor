package projektor.performance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import projektor.objectstore.json.JsonObjectStore
import projektor.persistence.objectstore.ObjectStoreKeys
import projektor.persistence.objectstore.PerformanceResultDocument
import projektor.persistence.objectstore.PerformanceResultsDocument
import projektor.persistence.objectstore.TestRunIndex
import projektor.persistence.objectstore.toStoredScale
import projektor.server.api.PublicId
import projektor.server.api.performance.PerformanceResult
import projektor.incomingresults.model.PerformanceResult as IncomingPerformanceResult

class PerformanceResultsObjectStoreRepository(
    private val store: JsonObjectStore,
    private val testRunIndex: TestRunIndex,
) : PerformanceResultsRepository {
    override suspend fun savePerformanceResults(
        publicId: PublicId,
        results: IncomingPerformanceResult,
    ): PerformanceResult {
        withContext(Dispatchers.IO) {
            val resultDocument =
                PerformanceResultDocument(
                    name = results.name,
                    requestsPerSecond = results.requestsPerSecond.toStoredScale(),
                    requestCount = results.requestCount,
                    average = results.average.toStoredScale(),
                    maximum = results.maximum.toStoredScale(),
                    p95 = results.p95.toStoredScale(),
                )

            store.update<PerformanceResultsDocument>(ObjectStoreKeys.performanceResults(publicId)) { current ->
                PerformanceResultsDocument(current?.results.orEmpty() + resultDocument)
            }
        }

        testRunIndex.refreshEntry(publicId)

        return results.toApi()
    }

    override suspend fun fetchResults(publicId: PublicId): List<PerformanceResult> =
        withContext(Dispatchers.IO) {
            store.read<PerformanceResultsDocument>(ObjectStoreKeys.performanceResults(publicId))
                ?.results
                ?.map {
                    PerformanceResult(
                        name = it.name,
                        requestsPerSecond = it.requestsPerSecond,
                        requestCount = it.requestCount,
                        average = it.average,
                        maximum = it.maximum,
                        p95 = it.p95,
                    )
                }
                ?: listOf()
        }
}
