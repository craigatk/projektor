package projektor.parser.coverage.model

data class CoverageReportFile(
    val directoryName: String,
    val fileName: String,
    val stats: CoverageReportStats,
    val missedLines: List<Int>,
    val partialLines: List<Int>
)
