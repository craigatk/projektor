package projektor.parser.coverage

import projektor.parser.coverage.clover.CloverXmlReportParser
import projektor.parser.coverage.cobertura.CoberturaXmlReportParser
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportType
import projektor.parser.jacoco.JacocoXmlReportParser

object CoverageParser {
    fun parseReport(reportXml: String, baseDirectoryPath: String?): CoverageReport? {
        val reportType = findReportType(reportXml)

        return when (reportType) {
            CoverageReportType.JACOCO -> JacocoCoverageReportParser().parseReport(reportXml, baseDirectoryPath)
            CoverageReportType.CLOVER -> CloverCoverageReportParser().parseReport(reportXml, baseDirectoryPath)
            CoverageReportType.COBERTURA -> CoberturaCoverageReportParser().parseReport(reportXml, baseDirectoryPath)
            else -> null
        }
    }

    private fun findReportType(reportXml: String): CoverageReportType? =
        when {
            JacocoXmlReportParser.isJacocoReport(reportXml) -> CoverageReportType.JACOCO
            CloverXmlReportParser.isCloverReport(reportXml) -> CoverageReportType.CLOVER
            CoberturaXmlReportParser.isCoberturaReport(reportXml) -> CoverageReportType.COBERTURA
            else -> null
        }
}
