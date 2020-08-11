package projektor.server.api.coverage

data class CoverageStats(
        val statementStat: CoverageStat,
        val lineStat: CoverageStat,
        val branchStat: CoverageStat
)
