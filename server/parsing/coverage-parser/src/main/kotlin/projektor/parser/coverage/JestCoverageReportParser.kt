package projektor.parser.coverage

import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats
import projektor.parser.jest.JestXmlReportParser

class JestCoverageReportParser {
    fun parseReport(reportXml: String): CoverageReport {
        val parsedReport = JestXmlReportParser().parseReport(reportXml)

        return CoverageReport(
            name = parsedReport.project.name,
            CoverageReportStats(
                lineStat = CoverageReportStat(
                    covered = parsedReport.project.metrics.coveredStatements,
                    missed = parsedReport.project.metrics.statements - parsedReport.project.metrics.coveredStatements
                ),
                branchStat = CoverageReportStat(
                    covered = parsedReport.project.metrics.coveredConditionals,
                    missed = parsedReport.project.metrics.conditionals - parsedReport.project.metrics.coveredConditionals
                ),
                statementStat = CoverageReportStat(covered = 0, missed = 0)
            )
        )
    }
}
