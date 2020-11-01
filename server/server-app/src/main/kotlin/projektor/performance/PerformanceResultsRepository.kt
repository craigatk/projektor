package projektor.performance

import projektor.parser.performance.model.PerformanceResultsReport
import projektor.server.api.PublicId

interface PerformanceResultsRepository {
    suspend fun savePerformanceResults(
        testRunId: Long,
        publicId: PublicId,
        name: String,
        results: PerformanceResultsReport
    )
}
