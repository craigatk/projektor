package projektor.parser.coverage

import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportType
import projektor.parser.jacoco.JacocoXmlReportParser
import projektor.parser.jest.JestXmlReportParser

object CoverageParser {
    fun parseReport(reportXml: String): CoverageReport? {
        val reportType = findReportType(reportXml)

        return when (reportType) {
            CoverageReportType.JACOCO -> JacocoCoverageReportParser().parseReport(reportXml)
            CoverageReportType.JEST -> JestCoverageReportParser().parseReport(reportXml)
            else -> null
        }
    }

    private fun findReportType(reportXml: String): CoverageReportType? =
            when {
                JacocoXmlReportParser.isJacocoReport(reportXml) -> {
                    CoverageReportType.JACOCO
                }
                JestXmlReportParser.isJestReport(reportXml) -> {
                    CoverageReportType.JEST
                }
                else -> {
                    null
                }
            }
}
