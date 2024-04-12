package projektor.notification.github.comment

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter

object GitHubCommentCreator {
    const val HEADER_TEXT = "Projektor reports"

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")

    fun appendComment(
        existingComment: String,
        report: ReportCommentData,
    ): String {
        return existingComment + "\n" + createReportTableRow(report)
    }

    fun createComment(report: ReportCommentData): String {
        return createReportTableHeader() + "\n" + createReportTableRow(report)
    }

    private fun createReportTableHeader(): String {
        return """
**$HEADER_TEXT**

| Projektor report | Result | Tests executed | Coverage | Project | Date | 
| ---------------- | ------ | -------------- | -------- | ------- | ---- |
            """.trimIndent().trim()
    }

    private fun createReportTableRow(report: ReportCommentData): String {
        val resultText = if (report.passed) "Passed" else "Failed"

        return "| [Projektor report](${createReportLink(
            report,
            "",
        )}) | $resultText | ${createTestsCellValue(
            report,
        )} | ${createCoverageCellValue(report)} | ${report.project ?: ""} | ${dateFormatter.format(report.createdDate)} UTC |"
    }

    private fun createReportLink(
        report: ReportCommentData,
        uri: String,
    ): String {
        val baseUrl =
            if (report.projektorServerBaseUrl.endsWith(
                    "/",
                )
            ) {
                report.projektorServerBaseUrl
            } else {
                "${report.projektorServerBaseUrl}/"
            }

        return "${baseUrl}tests/${report.publicId}/$uri"
    }

    private fun createTestsCellValue(report: ReportCommentData): String {
        val performanceData = report.performance

        return when {
            !performanceData.isNullOrEmpty() -> "[${createPerformanceCellValue(performanceData)}](${createReportLink(report, "")})"
            report.passed -> "[${report.totalTestCount} total](${createReportLink(report, "all")})"
            else -> "[${report.failedTestCount} failed](${createReportLink(
                report,
                "failed",
            )}) / [${report.totalTestCount} total](${createReportLink(report, "all")})"
        }
    }

    private fun createPerformanceCellValue(performanceDataList: List<ReportCommentPerformanceData>): String =
        if (performanceDataList.size == 1) {
            createPerformanceRow(performanceDataList[0])
        } else {
            performanceDataList.joinToString("<br />") { performanceData ->
                "${performanceData.name} - ${createPerformanceRow(performanceData)}"
            }
        }

    private fun createPerformanceRow(performanceData: ReportCommentPerformanceData) =
        "p95: ${performanceData.p95.setScale(
            0,
            RoundingMode.HALF_UP,
        )} ms, RPS: ${performanceData.requestsPerSecond.setScale(0, RoundingMode.HALF_UP)}"

    private fun createCoverageCellValue(report: ReportCommentData): String {
        val coverage = report.coverage

        return if (coverage != null) {
            val (lineCoveredPercentage, lineCoverageDelta) = report.coverage

            val deltaString =
                if (lineCoverageDelta != null) {
                    when {
                        lineCoverageDelta > BigDecimal.ZERO -> "(+$lineCoverageDelta%)"
                        lineCoverageDelta < BigDecimal.ZERO -> "($lineCoverageDelta%)"
                        else -> null
                    }
                } else {
                    null
                }

            val displayValue =
                if (deltaString != null) {
                    "$lineCoveredPercentage% $deltaString"
                } else {
                    "$lineCoveredPercentage%"
                }

            "[$displayValue](${createReportLink(report, "coverage")})"
        } else {
            ""
        }
    }
}
