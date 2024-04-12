package projektor.notification.github.comment

import java.math.BigDecimal

data class ReportCommentPerformanceData(
    val name: String,
    val requestsPerSecond: BigDecimal,
    val p95: BigDecimal,
)
