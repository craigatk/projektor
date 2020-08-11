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

        val statementCounter = findCounter(parsedReport, CounterType.INSTRUCTION)
        val lineCounter = findCounter(parsedReport, CounterType.LINE)
        val branchCounter = findCounter(parsedReport, CounterType.BRANCH)

        return CoverageReport(
                parsedReport.name,
                CoverageReportStats(
                        statementStat = CoverageReportStat(statementCounter.covered, statementCounter.missed),
                        lineStat = CoverageReportStat(lineCounter.covered, lineCounter.missed),
                        branchStat = CoverageReportStat(branchCounter.covered, branchCounter.missed)
                )
        )
    }

    companion object {
        private fun findCounter(parsedReport: Report, counterType: CounterType) =
                parsedReport.counters.find { it.type == counterType }
                        ?: throw IllegalStateException("Missing $counterType counter in Jacoco report ${parsedReport.name}")
    }
}