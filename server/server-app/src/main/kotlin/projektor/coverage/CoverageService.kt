package projektor.coverage

import projektor.parser.coverage.CoverageParser
import projektor.server.api.PublicId
import projektor.server.api.coverage.Coverage
import projektor.server.api.coverage.CoverageStats

class CoverageService(private val coverageRepository: CoverageRepository) {
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
                    groups = coverageReports.map { it.toCoverageGroup() },
                    overallStats = overallStats.toCoverageStats()
            )
        } else {
            null
        }
    }

    suspend fun getOverallStats(publicId: PublicId): CoverageStats? {
        val hasCoverageData = coverageExists(publicId)

        return if (hasCoverageData) {
            val overallReportStats = coverageRepository.fetchOverallStats(publicId)

            overallReportStats.toCoverageStats()
        } else {
            null
        }
    }

    suspend fun coverageExists(publicId: PublicId): Boolean =
            coverageRepository.coverageExists(publicId)
}
