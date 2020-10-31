package projektor.parser.coverage

import projektor.parser.coverage.clover.CloverXmlReportParser
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportType
import projektor.parser.jacoco.JacocoXmlReportParser

object CoverageParser {
    fun parseReport(reportXml: String): CoverageReport? {
        val reportType = findReportType(reportXml)

        return when (reportType) {
            CoverageReportType.JACOCO -> JacocoCoverageReportParser().parseReport(reportXml)
            CoverageReportType.CLOVER -> CloverCoverageReportParser().parseReport(reportXml)
            else -> null
        }
    }

    private fun findReportType(reportXml: String): CoverageReportType? =
        when {
            JacocoXmlReportParser.isJacocoReport(reportXml) -> {
                CoverageReportType.JACOCO
            }
            CloverXmlReportParser.isCloverReport(reportXml) -> {
                CoverageReportType.CLOVER
            }
            else -> {
                null
            }
        }
}
