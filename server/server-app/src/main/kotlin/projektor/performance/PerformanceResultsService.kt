package projektor.performance

import projektor.server.api.PublicId
import projektor.server.api.performance.PerformanceResult

class PerformanceResultsService(private val performanceResultsRepository: PerformanceResultsRepository) {
    suspend fun fetchResults(publicId: PublicId): List<PerformanceResult> =
        performanceResultsRepository.fetchResults(publicId)
}
