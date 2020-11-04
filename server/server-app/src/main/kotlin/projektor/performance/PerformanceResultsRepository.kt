package projektor.performance

import projektor.incomingresults.model.PerformanceResult
import projektor.server.api.PublicId

interface PerformanceResultsRepository {
    suspend fun savePerformanceResults(
        testRunId: Long,
        publicId: PublicId,
        results: PerformanceResult
    )
}
