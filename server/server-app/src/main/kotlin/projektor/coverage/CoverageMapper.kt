package projektor.coverage

import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportFile
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats
import projektor.server.api.coverage.Coverage
import projektor.server.api.coverage.CoverageFile
import projektor.server.api.coverage.CoverageGroup
import projektor.server.api.coverage.CoverageStat
import projektor.server.api.coverage.CoverageStats

fun CoverageReportStat.toCoverageStat(previousCoverageStats: CoverageStat?): CoverageStat =
    CoverageStat(
        covered = this.covered,
        missed = this.missed,
        coveredPercentageDelta = previousCoverageStats?.let { this.percentCovered - it.coveredPercentage }
    )

fun CoverageReportStats.toCoverageStats(previousCoverageStats: CoverageStats?): CoverageStats =
    CoverageStats(
        statementStat = this.statementStat.toCoverageStat(previousCoverageStats?.statementStat),
        lineStat = this.lineStat.toCoverageStat(previousCoverageStats?.lineStat),
        branchStat = this.branchStat.toCoverageStat(previousCoverageStats?.branchStat)
    )

fun CoverageReport.toCoverageGroup(previousCoverage: Coverage?): CoverageGroup =
    CoverageGroup(
        name = name,
        stats = totalStats.toCoverageStats(previousCoverage?.findCoverageGroup(name)?.stats)
    )

fun CoverageReportFile.toCoverageFile(): CoverageFile =
    CoverageFile(
        fileName = fileName,
        directoryName = directoryName,
        missedLines = missedLines.toTypedArray(),
        partialLines = partialLines.toTypedArray(),
        stats = stats.toCoverageStats(null),
        filePath = filePath
    )
