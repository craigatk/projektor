package projektor.cleanup

import org.slf4j.LoggerFactory
import projektor.attachment.AttachmentService
import projektor.coverage.CoverageService
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.server.api.PublicId
import projektor.server.api.results.ResultsProcessingStatus
import projektor.testrun.TestRunRepository
import java.time.LocalDate

class TestRunCleanupService(
    private val cleanupConfig: CleanupConfig,
    private val testRunRepository: TestRunRepository,
    private val resultsProcessingRepository: ResultsProcessingRepository,
    private val coverageService: CoverageService,
    private val attachmentService: AttachmentService?
) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    suspend fun conditionallyCleanupTestRuns(): List<PublicId> =
        if (cleanupConfig.reportCleanupEnabled) {
            val maxReportAgeDays = cleanupConfig.maxReportAgeDays?.toLong() ?: 0

            val createdBefore = LocalDate.now().minusDays(maxReportAgeDays)
            val testRunsToCleanUp = testRunRepository.findTestRunsCreatedBeforeAndNotPinned(createdBefore)

            val cleanedUpTestRunIds = testRunsToCleanUp.mapNotNull {
                conditionallyCleanupTestRun(it, cleanupConfig)
            }

            logger.info("Removed ${cleanedUpTestRunIds.size} test runs created before $createdBefore")

            cleanedUpTestRunIds
        } else {
            logger.info("Test run clean up not enabled, skipping")

            listOf()
        }

    suspend fun cleanupTestRun(publicId: PublicId) {
        testRunRepository.deleteTestRun(publicId)

        coverageService.deleteCoverage(publicId)

        attachmentService?.deleteAttachments(publicId)

        resultsProcessingRepository.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.DELETED)
    }

    private suspend fun conditionallyCleanupTestRun(publicId: PublicId, cleanupConfig: CleanupConfig): PublicId? =
        try {
            if (!cleanupConfig.dryRun) {
                cleanupTestRun(publicId)
            } else {
                logger.info("Not deleting test run $publicId since dry run is enabled")
            }
            publicId
        } catch (e: Exception) {
            logger.error("Failed to clean up test run $publicId", e)
            null
        }
}
