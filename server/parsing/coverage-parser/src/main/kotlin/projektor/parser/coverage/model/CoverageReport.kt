package projektor.parser.coverage.model

data class CoverageReport(
    val name: String,
    val totalStats: CoverageReportStats,
    val files: List<CoverageReportFile>?,
)
