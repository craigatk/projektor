package projektor.coverage

import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats
import projektor.server.api.coverage.CoverageStat
import projektor.server.api.coverage.CoverageStats

fun CoverageReportStat.toCoverageStat(): CoverageStat =
        CoverageStat(
                covered = this.covered,
                missed = this.missed,
                total = this.total,
                coveredPercentage = this.percentCovered
        )

fun CoverageReportStats.toCoverageStats(): CoverageStats =
        CoverageStats(
                statementStat = this.statementStat.toCoverageStat(),
                lineStat = this.lineStat.toCoverageStat(),
                branchStat = this.branchStat.toCoverageStat()
        )
