package projektor.notification.github.comment

import java.time.LocalDateTime

data class ReportCommentData(
    val projektorServerBaseUrl: String,
    val git: ReportCommentGitData,
    val publicId: String,
    val createdDate: LocalDateTime,
    val passed: Boolean,
    val failedTestCount: Int,
    val totalTestCount: Int,
    val coverage: ReportCoverageCommentData?,
    val performance: List<ReportCommentPerformanceData>?,
    val project: String?,
)
