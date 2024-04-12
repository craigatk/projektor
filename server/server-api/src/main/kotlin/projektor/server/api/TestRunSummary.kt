package projektor.server.api

import java.math.BigDecimal
import java.time.Instant

data class TestRunSummary(
    val id: String,
    val totalTestCount: Int,
    val totalPassingCount: Int,
    val totalSkippedCount: Int,
    val totalFailureCount: Int,
    val passed: Boolean,
    val cumulativeDuration: BigDecimal,
    val averageDuration: BigDecimal,
    val slowestTestCaseDuration: BigDecimal,
    val createdTimestamp: Instant,
    val wallClockDuration: BigDecimal?,
)
