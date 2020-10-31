package projektor.parser.performance.model

import java.math.BigDecimal

data class PerformanceStats(
    val average: BigDecimal,
    val maximum: BigDecimal,
    val p95: BigDecimal
)
