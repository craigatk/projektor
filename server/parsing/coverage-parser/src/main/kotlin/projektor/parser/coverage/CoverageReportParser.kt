package projektor.parser.coverage

import projektor.parser.coverage.model.CoverageReport

interface CoverageReportParser {
    fun parseReport(reportXml: String, baseDirectoryPath: String?): CoverageReport
}
