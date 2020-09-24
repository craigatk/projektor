package projektor.server.api.repository

import java.math.BigDecimal
import java.time.Instant

data class RepositoryTestRunTimelineEntry(
    val publicId: String,
    val createdTimestamp: Instant,
    val cumulativeDuration: BigDecimal,
    val wallClockDuration: BigDecimal?,
    val totalTestCount: Int,
    val passed: Boolean
)
