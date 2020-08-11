package projektor.coverage

import projektor.parser.coverage.CoverageParser
import projektor.server.api.PublicId
import projektor.server.api.coverage.CoverageStats

class CoverageService(private val coverageRepository: CoverageRepository) {
    suspend fun saveReport(reportXml: String, publicId: PublicId) {
        val coverageRun = coverageRepository.createOrGetCoverageRun(publicId)
        val coverageReport = CoverageParser.parseReport(reportXml)

        coverageReport?.let { coverageRepository.addCoverageReport(coverageRun, it) }
    }

    fun getOverallStats(publicId: PublicId): CoverageStats? {
        return null
    }
}