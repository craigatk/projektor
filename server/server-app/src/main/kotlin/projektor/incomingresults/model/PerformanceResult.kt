package projektor.incomingresults.model

import java.math.BigDecimal

data class PerformanceResult(
    val name: String,
    val requestsPerSecond: BigDecimal,
    val requestCount: Long,
    val average: BigDecimal,
    val maximum: BigDecimal,
    val p95: BigDecimal
)
