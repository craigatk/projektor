package projektor.coverage

import org.slf4j.LoggerFactory
import projektor.compare.PreviousTestRunService
import projektor.error.FailureBodyType
import projektor.error.ProcessingFailureService
import projektor.parser.coverage.CoverageParser
import projektor.parser.coverage.model.CoverageReport
import projektor.server.api.PublicId
import projektor.server.api.coverage.Coverage
import projektor.server.api.coverage.CoverageFile
import projektor.server.api.coverage.CoverageStats

class CoverageService(
    private val coverageRepository: CoverageRepository,
    private val previousTestRunService: PreviousTestRunService,
    private val processingFailureService: ProcessingFailureService
) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    suspend fun saveReport(reportXml: String, publicId: PublicId): CoverageReport? =
        try {
            val coverageRun = coverageRepository.createOrGetCoverageRun(publicId)
            val coverageReport = CoverageParser.parseReport(reportXml)

            coverageReport?.let {
                val coverageGroup = coverageRepository.addCoverageReport(coverageRun, it)

                val coverageFiles = coverageReport.files

                if (coverageFiles != null && coverageFiles.isNotEmpty()) {
                    coverageRepository.insertCoverageFiles(
                        coverageFiles,
                        coverageRun,
                        coverageGroup
                    )
                }
            }

            coverageReport
        } catch (e: Exception) {
            logger.error("Error saving coverage report", e)
            processingFailureService.recordProcessingFailure(
                publicId = publicId,
                body = reportXml,
                bodyType = FailureBodyType.COVERAGE,
                e = e
            )
            throw e
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

    suspend fun getCoverageGroupFiles(publicId: PublicId, groupName: String): List<CoverageFile> =
        coverageRepository.fetchCoverageFiles(publicId, groupName)
}
