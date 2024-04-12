package projektor.parser.coverage.model

data class CoverageReportStats(
    val statementStat: CoverageReportStat,
    val lineStat: CoverageReportStat,
    val branchStat: CoverageReportStat,
)
