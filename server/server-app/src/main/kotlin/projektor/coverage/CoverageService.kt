package projektor.coverage

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import projektor.compare.PreviousTestRunService
import projektor.error.ProcessingFailureService
import projektor.metrics.MetricsService
import projektor.parser.coverage.CoverageParseException
import projektor.parser.coverage.CoverageParser
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.parser.coverage.payload.CoveragePayloadParser
import projektor.server.api.PublicId
import projektor.server.api.coverage.Coverage
import projektor.server.api.coverage.CoverageFile
import projektor.server.api.coverage.CoverageStat
import projektor.server.api.coverage.CoverageStats
import projektor.server.api.error.FailureBodyType
import java.math.BigDecimal

class CoverageService(
    private val coverageRepository: CoverageRepository,
    private val metricsService: MetricsService,
    private val previousTestRunService: PreviousTestRunService,
    private val processingFailureService: ProcessingFailureService
) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)
    private val coveragePayloadParser = CoveragePayloadParser()

    suspend fun parseAndSaveReport(payload: String, publicId: PublicId): CoverageReport? =
        try {
            val coverageFilePayload = coveragePayloadParser.parseCoverageFilePayload(payload)

            saveReportInternal(coverageFilePayload, publicId)
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

    suspend fun saveReport(coverageFilePayload: CoverageFilePayload, publicId: PublicId): CoverageReport? =
        try {
            saveReportInternal(coverageFilePayload, publicId)
        } catch (e: Exception) {
            when (e) {
                is JsonProcessingException, is CoverageParseException -> {
                    logger.info("Problem parsing coverage report", e)
                    metricsService.incrementCoverageParseFailureCounter()
                }
                else -> {
                    logger.error("Error saving coverage report", e)
                    metricsService.incrementCoverageProcessFailureCounter()
                }
            }

            processingFailureService.recordProcessingFailure(
                publicId = publicId,
                body = coverageFilePayload.reportContents,
                bodyType = FailureBodyType.COVERAGE,
                e = e
            )
            throw e
        }

    private suspend fun saveReportInternal(coverageFilePayload: CoverageFilePayload, publicId: PublicId): CoverageReport? {
        metricsService.incrementCoverageProcessStartCounter()
        val coverageRun = coverageRepository.createOrGetCoverageRun(publicId)
        val coverageReport = CoverageParser.parseReport(coverageFilePayload.reportContents, coverageFilePayload.baseDirectoryPath)

        coverageReport?.let {
            val coverageGroup = coverageRepository.addCoverageReport(coverageRun, it)

            val coverageFiles = coverageReport.files

            if (coverageFiles != null && coverageFiles.isNotEmpty()) {
                coverageRepository.insertCoverageFiles(
                    coverageFiles.map { file -> file.toCoverageFile() },
                    coverageRun,
                    coverageGroup,
                )
            }

            metricsService.incrementCoverageProcessSuccessCounter()
        }

        return coverageReport
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
        coverageRepository.coverageGroupExists(publicId)

    suspend fun deleteCoverage(publicId: PublicId) =
        coverageRepository.deleteCoverage(publicId)

    suspend fun getCoverageGroupFiles(publicId: PublicId, groupName: String): List<CoverageFile> =
        coverageRepository.fetchCoverageFiles(publicId, groupName)

    suspend fun parseReport(coverageFilePayload: CoverageFilePayload): Pair<CoverageReport, List<CoverageFile>>? =
        withContext(Dispatchers.IO) {
            val coverageReport = CoverageParser.parseReport(coverageFilePayload.reportContents, coverageFilePayload.baseDirectoryPath)

            coverageReport?.let { report ->
                val coverageFiles = report.files?.let { fileList ->
                    fileList.map { it.toCoverageFile() }
                } ?: listOf()

                Pair(coverageReport, coverageFiles)
            }
        }

    suspend fun appendCoverage(publicId: PublicId, coverageFiles: List<CoverageFilePayload>) {
        val coverageRun = coverageRepository.createOrGetCoverageRun(publicId)

        coverageFiles.forEach { coverageFilePayload ->
            val parsedReport = try {
                parseReport(coverageFilePayload)
            } catch (e: Exception) {
                logger.info("Problem parsing coverage report", e)
                metricsService.incrementCoverageParseFailureCounter()

                processingFailureService.recordProcessingFailure(
                    publicId = publicId,
                    body = coverageFilePayload.reportContents,
                    bodyType = FailureBodyType.COVERAGE,
                    e = e
                )
                null
            }

            if (parsedReport != null) {
                val (incomingCoverageReport, incomingCoverageFiles) = parsedReport

                try {
                    val existingCoverageFiles =
                        coverageRepository.fetchCoverageFiles(publicId, incomingCoverageReport.name)

                    val combinedCoverageFiles = combineCoverageFiles(existingCoverageFiles, incomingCoverageFiles)

                    val coveredLines = combinedCoverageFiles.sumOf { it.stats.lineStat.covered }
                    val missedLines = combinedCoverageFiles.sumOf { it.stats.lineStat.missed }
                    val newLineStat =
                        CoverageStat(covered = coveredLines, missed = missedLines, coveredPercentageDelta = null)

                    val (coverageGroup, coverageGroupStatus) = coverageRepository.upsertCoverageGroup(
                        coverageRun,
                        incomingCoverageReport,
                        newLineStat
                    )

                    if (coverageGroupStatus == CoverageGroupStatus.NEW) {
                        coverageRepository.insertCoverageFiles(
                            incomingCoverageFiles,
                            coverageRun,
                            coverageGroup,
                        )
                    } else {
                        coverageRepository.upsertCoverageFiles(combinedCoverageFiles, coverageRun, coverageGroup)
                    }
                } catch (e: Exception) {
                    logger.error("Error saving coverage report", e)
                    metricsService.incrementCoverageProcessFailureCounter()

                    processingFailureService.recordProcessingFailure(
                        publicId = publicId,
                        body = coverageFilePayload.reportContents,
                        bodyType = FailureBodyType.COVERAGE,
                        e = e
                    )
                }
            }
        }
    }
}
