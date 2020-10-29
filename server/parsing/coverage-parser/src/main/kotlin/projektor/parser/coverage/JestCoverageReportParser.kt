package projektor.parser.coverage

import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportFile
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats
import projektor.parser.jest.JestXmlReportParser
import projektor.parser.jest.model.LineType
import projektor.parser.jest.model.Metrics

class JestCoverageReportParser {
    fun parseReport(reportXml: String): CoverageReport {
        val parsedReport = JestXmlReportParser().parseReport(reportXml)

        val files = parsedReport.project.packages.flatMap { pkg ->
            pkg.files.map { sourceFile ->
                CoverageReportFile(
                    directoryName = pkg.name,
                    fileName = sourceFile.name,
                    missedLines = sourceFile.lines?.filter { it.lineCoverageType() == LineType.MISSED }?.map { it.number } ?: listOf(),
                    partialLines = sourceFile.lines?.filter { it.lineCoverageType() == LineType.PARTIAL }?.map { it.number } ?: listOf(),
                    stats = createStats(sourceFile.metrics)
                )
            }
        }

        return CoverageReport(
            name = parsedReport.project.name,
            totalStats = createStats(parsedReport.project.metrics),
            files = files
        )
    }

    companion object {
        private fun createStats(metrics: Metrics): CoverageReportStats =
            CoverageReportStats(
                lineStat = CoverageReportStat(
                    covered = metrics.coveredStatements,
                    missed = metrics.statements - metrics.coveredStatements
                ),
                branchStat = CoverageReportStat(
                    covered = metrics.coveredConditionals,
                    missed = metrics.conditionals - metrics.coveredConditionals
                ),
                statementStat = CoverageReportStat(covered = 0, missed = 0)
            )
    }
}
