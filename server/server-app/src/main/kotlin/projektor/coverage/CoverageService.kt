package projektor.coverage

import org.slf4j.LoggerFactory
import projektor.compare.PreviousTestRunService
import projektor.error.FailureBodyType
import projektor.error.ProcessingFailureService
import projektor.parser.coverage.CoverageParser
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.parser.coverage.payload.CoveragePayloadParser
import projektor.server.api.PublicId
import projektor.server.api.coverage.Coverage
import projektor.server.api.coverage.CoverageFile
import projektor.server.api.coverage.CoverageStats
import java.math.BigDecimal

class CoverageService(
    private val coverageRepository: CoverageRepository,
    private val previousTestRunService: PreviousTestRunService,
    private val processingFailureService: ProcessingFailureService
) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)
    private val coveragePayloadParser = CoveragePayloadParser()

    suspend fun parseAndSaveReport(payload: String, publicId: PublicId): CoverageReport? =
        try {
            val coverageFilePayload = coveragePayloadParser.parseCoverageFilePayload(payload)

            val coverageReport = CoverageParser.parseReport(coverageFilePayload.reportContents, coverageFilePayload.baseDirectoryPath)

            coverageReport?.let { saveReport(coverageReport, publicId) }

            coverageReport
        } catch (e: Exception) {
            logger.error("Error saving coverage report", e)
            processingFailureService.recordProcessingFailure(
                publicId = publicId,
                body = payload,
                bodyType = FailureBodyType.COVERAGE,
                e = e
            )
            throw e
        }

    suspend fun parseAndSaveReports(coverageFilePayload: List<CoverageFilePayload>, publicId: PublicId): List<CoverageReport> {
        return listOf()
    }

    suspend fun parseAndSaveReport(coverageFilePayload: CoverageFilePayload, publicId: PublicId): CoverageReport? =
        try {
            val coverageReport = CoverageParser.parseReport(coverageFilePayload.reportContents, coverageFilePayload.baseDirectoryPath)

            coverageReport?.let { saveReport(coverageReport, publicId) }

            coverageReport
        } catch (e: Exception) {
            logger.error("Error saving coverage report", e)
            processingFailureService.recordProcessingFailure(
                publicId = publicId,
                body = coverageFilePayload.reportContents,
                bodyType = FailureBodyType.COVERAGE,
                e = e
            )
            throw e
        }

    private suspend fun saveReport(coverageReport: CoverageReport, publicId: PublicId) {
        val coverageRun = coverageRepository.createOrGetCoverageRun(publicId)

        val coverageGroup = coverageRepository.addCoverageReport(coverageRun, coverageReport)

        val coverageFiles = coverageReport.files

        if (coverageFiles != null && coverageFiles.isNotEmpty()) {
            coverageRepository.insertCoverageFiles(
                coverageFiles,
                coverageRun,
                coverageGroup,
            )
        }
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

    suspend fun getCoveredLinePercentage(publicId: PublicId): BigDecimal? =
        getCoverage(publicId)
            ?.overallStats
            ?.lineStat
            ?.coveredPercentage

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
