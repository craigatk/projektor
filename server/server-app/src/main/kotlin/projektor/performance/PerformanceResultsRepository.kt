package projektor.performance

import projektor.server.api.PublicId
import projektor.server.api.performance.PerformanceResult
import projektor.incomingresults.model.PerformanceResult as IncomingPerformanceResult

interface PerformanceResultsRepository {
    suspend fun savePerformanceResults(
        testRunId: Long,
        publicId: PublicId,
        results: IncomingPerformanceResult,
    ): PerformanceResult

    suspend fun fetchResults(publicId: PublicId): List<PerformanceResult>
}
