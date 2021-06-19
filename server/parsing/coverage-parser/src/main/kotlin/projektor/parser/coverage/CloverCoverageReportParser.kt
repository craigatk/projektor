package projektor.parser.coverage

import projektor.parser.coverage.clover.CloverXmlReportParser
import projektor.parser.coverage.clover.model.LineType
import projektor.parser.coverage.clover.model.Metrics
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportFile
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats

class CloverCoverageReportParser : CoverageReportParser {
    override fun parseReport(reportXml: String, baseDirectoryPath: String?): CoverageReport {
        val parsedReport = CloverXmlReportParser().parseReport(reportXml)

        val files = parsedReport.project.packages.flatMap { pkg ->
            pkg.files.map { sourceFile ->
                val directoryName = pkg.name.replace(".", "/")
                val fileName = sourceFile.name
                val filePath = if (baseDirectoryPath != null) "$baseDirectoryPath/$directoryName/$fileName" else "$directoryName/$fileName"

                CoverageReportFile(
                    directoryName = directoryName,
                    fileName = fileName,
                    filePath = filePath,
                    missedLines = sourceFile.lines?.filter { it.lineCoverageType() == LineType.MISSED }?.map { it.number } ?: listOf(),
                    partialLines = listOf(), // Clover partial lines from Jest coverage reports is not accurate so don't show anything rather than inaccurate data
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
