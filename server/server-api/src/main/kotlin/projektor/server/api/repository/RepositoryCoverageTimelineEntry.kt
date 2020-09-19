package projektor.server.api.repository

import java.time.Instant
import projektor.server.api.coverage.CoverageStats

data class RepositoryCoverageTimelineEntry(
    val publicId: String,
    val createdTimestamp: Instant,
    val coverageStats: CoverageStats
)
