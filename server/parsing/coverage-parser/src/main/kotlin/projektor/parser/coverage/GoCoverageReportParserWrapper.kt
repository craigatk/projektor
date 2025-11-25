package projektor.parser.coverage

import projektor.parser.coverage.go.GoCoverageReportParser
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportFile
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats

class GoCoverageReportParserWrapper : CoverageReportParser {
    override fun parseReport(
        reportXml: String,
        baseDirectoryPath: String?,
    ): CoverageReport {
        val parsedReport = GoCoverageReportParser.parseReport(reportXml)

        val files: List<CoverageReportFile> =
            parsedReport.files.map { file ->
                val coveredLines = file.coveredLines
                val missedLines = file.missedLines
                val partialLines = file.partialLines

                CoverageReportFile(
                    directoryName = file.directoryName,
                    fileName = file.fileName,
                    filePath = file.filePath,
                    missedLines = missedLines.toList().sorted(),
                    partialLines = partialLines.toList().sorted(),
                    stats =
                        CoverageReportStats(
                            lineStat =
                                CoverageReportStat(
                                    covered = coveredLines.size,
                                    missed = missedLines.size,
                                ),
                            branchStat = CoverageReportStat(0, 0),
                            statementStat =
                                CoverageReportStat(
                                    covered = file.coveredStatements,
                                    missed = file.missedStatements,
                                ),
                        ),
                )
            }

        val totalCoveredLines = files.sumOf { it.stats.lineStat.covered }
        val totalMissedLines = files.sumOf { it.stats.lineStat.missed }
        val totalCoveredStatements = files.sumOf { it.stats.statementStat.covered }
        val totalMissedStatements = files.sumOf { it.stats.statementStat.missed }

        return CoverageReport(
            name = "Coverage",
            totalStats =
                CoverageReportStats(
                    lineStat =
                        CoverageReportStat(
                            covered = totalCoveredLines,
                            missed = totalMissedLines,
                        ),
                    branchStat = CoverageReportStat(0, 0),
                    statementStat =
                        CoverageReportStat(
                            covered = totalCoveredStatements,
                            missed = totalMissedStatements,
                        ),
                ),
            files = files,
        )
    }
}
