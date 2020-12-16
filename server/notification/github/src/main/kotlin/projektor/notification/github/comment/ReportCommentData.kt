package projektor.notification.github.comment

import java.time.LocalDateTime

data class ReportCommentData(
    val serverBaseUrl: String,
    val publicId: String,
    val createdDate: LocalDateTime,
    val passed: Boolean,
    val failedTestCount: Int,
    val totalTestCount: Int,
    val coverage: ReportCoverageCommentData?,
    val project: String?
)
