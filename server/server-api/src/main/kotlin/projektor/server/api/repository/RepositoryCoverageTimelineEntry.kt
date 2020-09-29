package projektor.server.api.repository

import projektor.server.api.coverage.CoverageStats
import java.time.Instant

data class RepositoryCoverageTimelineEntry(
    val publicId: String,
    val createdTimestamp: Instant,
    val coverageStats: CoverageStats
)
