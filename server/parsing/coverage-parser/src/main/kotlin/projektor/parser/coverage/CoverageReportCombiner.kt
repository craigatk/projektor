package projektor.parser.coverage

import projektor.parser.coverage.CoverageReportFilesCombiner.combineFilesIntoReport
import projektor.parser.coverage.model.CoverageReport

object CoverageReportCombiner {
    fun combineCoverageReports(coverageReports: List<CoverageReport>): List<CoverageReport> {
        val uniqueReportNames = coverageReports.map { it.name }.distinct()

        if (uniqueReportNames.size == coverageReports.size) {
            return coverageReports
        }

        return uniqueReportNames.map { reportName ->
            val reportsWithName = coverageReports.filter { it.name == reportName }

            if (reportsWithName.size == 1) {
                reportsWithName[0]
            } else {
                val allReportFiles = reportsWithName.mapNotNull { it.files }.flatten()

                combineFilesIntoReport(reportName, allReportFiles)
            }
        }
    }
}