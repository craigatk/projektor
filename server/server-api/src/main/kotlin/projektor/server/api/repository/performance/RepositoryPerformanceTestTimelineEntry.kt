package projektor.server.api.repository.performance

import projektor.server.api.performance.PerformanceResult
import java.time.Instant

data class RepositoryPerformanceTestTimelineEntry(
    val publicId: String,
    val createdTimestamp: Instant,
    val performanceResult: PerformanceResult
)
