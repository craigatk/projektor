package projektor.notification.github.comment

import java.math.BigDecimal

data class ReportCoverageCommentData(
    val lineCoveredPercentage: BigDecimal,
    val lineCoverageDelta: BigDecimal?,
)
