package projektor.parser.coverage

import projektor.parser.coverage.CoverageReportCombiner.combineCoverageReports
import projektor.parser.coverage.clover.CloverXmlReportParser
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportType
import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.parser.jacoco.JacocoXmlReportParser

object CoverageParser {
    fun parseReports(coverageFilePayloads: List<CoverageFilePayload>): List<CoverageParseResult> {

        val results = coverageFilePayloads.map { payload ->
            try {
                val report = parseReport(payload.reportContents, payload.baseDirectoryPath)

                report?.let { CoverageParseResult.Success(it) } ?: CoverageParseResult.Failure(null)
            } catch (e: Exception) {
                CoverageParseResult.Failure(e)
            }
        }

        val successfulResults = results.filterIsInstance<CoverageParseResult.Success>()
        val failedResults = results.filterIsInstance<CoverageParseResult.Failure>()

        val combinedSuccessfulResults = combineCoverageReports(successfulResults.map { it.coverageReport} )
                .map { CoverageParseResult.Success(it) }

        return combinedSuccessfulResults + failedResults
    }

    fun parseReport(reportXml: String, baseDirectoryPath: String?): CoverageReport? {
        return when (findReportType(reportXml)) {
            CoverageReportType.JACOCO -> JacocoCoverageReportParser().parseReport(reportXml, baseDirectoryPath)
            CoverageReportType.CLOVER -> CloverCoverageReportParser().parseReport(reportXml, baseDirectoryPath)
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
