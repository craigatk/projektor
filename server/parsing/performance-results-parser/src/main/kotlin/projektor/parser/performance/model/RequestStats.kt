package projektor.parser.performance.model

import java.math.BigDecimal

data class RequestStats(
    val ratePerSecond: BigDecimal,
    val count: Long,
)
