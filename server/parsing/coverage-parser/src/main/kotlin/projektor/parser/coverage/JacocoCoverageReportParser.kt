package projektor.parser.coverage

import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats
import projektor.parser.jacoco.JacocoXmlReportParser
import projektor.parser.jacoco.model.CounterType
import projektor.parser.jacoco.model.Report

class JacocoCoverageReportParser {
    fun parseReport(reportXml: String): CoverageReport {
        val parsedReport = JacocoXmlReportParser().parseReport(reportXml)

        val statementStat = createStat(parsedReport, CounterType.INSTRUCTION)
        val lineStat = createStat(parsedReport, CounterType.LINE)
        val branchStat = createStat(parsedReport, CounterType.BRANCH)

        return CoverageReport(
            parsedReport.name,
            CoverageReportStats(
                statementStat = statementStat,
                lineStat = lineStat,
                branchStat = branchStat
            )
        )
    }

    companion object {
        private fun createStat(parsedReport: Report, counterType: CounterType): CoverageReportStat {
            val counter = parsedReport.counters.find { it.type == counterType }

            return if (counter != null) {
                CoverageReportStat(covered = counter.covered, missed = counter.missed)
            } else {
                CoverageReportStat(0, 0)
            }
        }
    }
}
