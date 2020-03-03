package projektor.server.api

import java.math.BigDecimal
import java.time.LocalDateTime

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
    val createdTimestamp: LocalDateTime?
)
