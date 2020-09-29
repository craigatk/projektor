package projektor.coverage

import projektor.compare.PreviousTestRunService
import projektor.parser.coverage.CoverageParser
import projektor.server.api.PublicId
import projektor.server.api.coverage.Coverage
import projektor.server.api.coverage.CoverageStats

class CoverageService(
    private val coverageRepository: CoverageRepository,
    private val previousTestRunService: PreviousTestRunService
) {
    suspend fun saveReport(reportXml: String, publicId: PublicId) {
        val coverageRun = coverageRepository.createOrGetCoverageRun(publicId)
        val coverageReport = CoverageParser.parseReport(reportXml)

        coverageReport?.let { coverageRepository.addCoverageReport(coverageRun, it) }
    }

    suspend fun getCoverage(publicId: PublicId): Coverage? {
        val hasCoverageData = coverageExists(publicId)

        return if (hasCoverageData) {
            val coverageReports = coverageRepository.fetchCoverageList(publicId)
            val overallStats = coverageRepository.fetchOverallStats(publicId)

            Coverage(
                groups = coverageReports.map { it.toCoverageGroup(null) },
                overallStats = overallStats.toCoverageStats(null),
                previousTestRunId = null
            )
        } else {
            null
        }
    }

    suspend fun getCoverageWithPreviousRunComparison(publicId: PublicId): Coverage? {
        val hasCoverageData = coverageExists(publicId)

        return if (hasCoverageData) {
            val coverageReports = coverageRepository.fetchCoverageList(publicId)
            val overallStats = coverageRepository.fetchOverallStats(publicId)

            val previousTestRun = previousTestRunService.findPreviousMainBranchRunWithCoverage(publicId)
            val previousCoverage: Coverage? = previousTestRun?.let { getCoverage(it) }

            Coverage(
                groups = coverageReports.map { it.toCoverageGroup(previousCoverage) },
                overallStats = overallStats.toCoverageStats(previousCoverage?.overallStats),
                previousTestRunId = previousTestRun?.id
            )
        } else {
            null
        }
    }

    suspend fun getOverallStats(publicId: PublicId): CoverageStats? {
        val hasCoverageData = coverageExists(publicId)

        return if (hasCoverageData) {
            val overallReportStats = coverageRepository.fetchOverallStats(publicId)

            overallReportStats.toCoverageStats(null)
        } else {
            null
        }
    }

    suspend fun coverageExists(publicId: PublicId): Boolean =
        coverageRepository.coverageExists(publicId)

    suspend fun deleteCoverage(publicId: PublicId) =
        coverageRepository.deleteCoverage(publicId)
}
