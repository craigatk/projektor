package projektor.parser.coverage

import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportFile
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats
import projektor.parser.jacoco.JacocoXmlReportParser
import projektor.parser.jacoco.model.Counter
import projektor.parser.jacoco.model.CounterType
import projektor.parser.jacoco.model.LineType

class JacocoCoverageReportParser : CoverageReportParser {
    override fun parseReport(
        reportXml: String,
        baseDirectoryPath: String?,
    ): CoverageReport =
        try {
            val parsedReport = JacocoXmlReportParser().parseReport(reportXml)

            val files =
                parsedReport.packages?.flatMap { pkg ->
                    pkg.sourceFiles.map { sourceFile ->
                        val directoryName = pkg.name.replace(".", "/")
                        val fileName = sourceFile.name
                        val filePath =
                            if (baseDirectoryPath != null) "$baseDirectoryPath/$directoryName/$fileName" else null

                        CoverageReportFile(
                            directoryName = directoryName,
                            fileName = fileName,
                            missedLines =
                                sourceFile.lines?.filter { it.lineType() == LineType.MISSED }?.map { it.number }
                                    ?: listOf(),
                            partialLines =
                                sourceFile.lines?.filter { it.lineType() == LineType.PARTIAL }?.map { it.number }
                                    ?: listOf(),
                            stats = createStats(sourceFile.counters),
                            filePath = filePath,
                        )
                    }
                }

            CoverageReport(
                parsedReport.name,
                createStats(parsedReport.counters),
                files,
            )
        } catch (e: Exception) {
            throw CoverageParseException(e)
        }

    companion object {
        private fun createStats(counters: List<Counter>?): CoverageReportStats {
            val statementStat = createStat(counters, CounterType.INSTRUCTION)
            val lineStat = createStat(counters, CounterType.LINE)
            val branchStat = createStat(counters, CounterType.BRANCH)

            return CoverageReportStats(
                statementStat = statementStat,
                lineStat = lineStat,
                branchStat = branchStat,
            )
        }

        private fun createStat(
            counters: List<Counter>?,
            counterType: CounterType,
        ): CoverageReportStat {
            val counter = counters?.find { it.type == counterType }

            return if (counter != null) {
                CoverageReportStat(covered = counter.covered, missed = counter.missed)
            } else {
                CoverageReportStat(0, 0)
            }
        }
    }
}
