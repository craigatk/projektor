package projektor.parser.coverage

import projektor.parser.coverage.cobertura.CoberturaXmlReportParser
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportFile
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats

class CoberturaCoverageReportParser : CoverageReportParser {
    override fun parseReport(reportXml: String, baseDirectoryPath: String?): CoverageReport {
        val parsedReport = CoberturaXmlReportParser().parseReport(reportXml)

        val files: List<CoverageReportFile> = parsedReport.packages.packages.flatMap { pkg ->
            val classes = pkg.classes.clazz

            classes.map { clazz ->
                val lines = clazz.lines.lines

                CoverageReportFile(
                    directoryName = clazz.fileName.substringBeforeLast(clazz.name).removeSuffix("/"),
                    fileName = clazz.name,
                    filePath = clazz.fileName,
                    missedLines = lines.filter { !it.isCovered }.map { it.number },
                    partialLines = lines.filter { it.isPartial }.map { it.number },
                    stats = CoverageReportStats(
                        lineStat = CoverageReportStat(
                            covered = lines.count { it.isCovered },
                            missed = lines.count { !it.isCovered }
                        ),
                        branchStat = CoverageReportStat(0, 0),
                        statementStat = CoverageReportStat(0, 0)
                    )
                )
            }
        }

        return CoverageReport(
            name = "Coverage",
            totalStats = CoverageReportStats(
                lineStat = CoverageReportStat(
                    covered = parsedReport.linesCovered,
                    missed = parsedReport.linesValid - parsedReport.linesCovered
                ),
                branchStat = CoverageReportStat(
                    covered = parsedReport.branchesCovered,
                    missed = parsedReport.branchesValid - parsedReport.branchesCovered
                ),
                statementStat = CoverageReportStat(0, 0)
            ),
            files = files
        )
    }
}
