package projektor.notification.github.comment

import java.time.format.DateTimeFormatter

object GitHubCommentCreator {
    private const val headerText = "Projektor reports:"

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")

    fun appendComment(existingComment: String, report: ReportCommentData): String {
        return ""
    }

    fun createComment(report: ReportCommentData): String {
        return createReportTableHeader() + "\n" + createReportTableRow(report)
    }

    fun isReportComment(commentBody: String): Boolean = commentBody.contains(headerText)

    private fun createReportTableHeader(): String {
        return """
$headerText

| Projektor report | Result | Tests | Coverage | Project | Date | 
| ---------------- | ------ | ----- | -------- | ------- | ---- |
        """.trimIndent().trim()
    }

    private fun createReportTableRow(report: ReportCommentData): String {
        val resultText = if (report.passed) "Passed" else "Failed"
        val testText = if (report.passed)
            "[${report.totalTestCount} total](${createReportLink(report, "all")})"
        else
            "[${report.failedTestCount} failed](${createReportLink(report, "failed")}) / [${report.totalTestCount} total](${createReportLink(report, "all")})"

        return "| [Projektor report](${createReportLink(report, "")}) | $resultText | $testText | | | ${dateFormatter.format(report.createdDate)} UTC |"
    }

    private fun createReportLink(report: ReportCommentData, uri: String): String {
        val baseUrl = if (report.serverBaseUrl.endsWith("/")) report.serverBaseUrl else "${report.serverBaseUrl}/"

        return "${baseUrl}tests/${report.publicId}/$uri"
    }
}
