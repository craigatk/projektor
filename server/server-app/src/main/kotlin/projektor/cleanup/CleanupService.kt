package projektor.cleanup

import io.ktor.util.KtorExperimentalAPI
import java.lang.Exception
import java.time.LocalDate
import org.slf4j.LoggerFactory
import projektor.attachment.AttachmentService
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.server.api.PublicId
import projektor.server.api.results.ResultsProcessingStatus
import projektor.testrun.TestRunRepository

@KtorExperimentalAPI
class CleanupService(
    private val cleanupConfig: CleanupConfig,
    private val testRunRepository: TestRunRepository,
    private val resultsProcessingRepository: ResultsProcessingRepository,
    private val attachmentService: AttachmentService?
) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    suspend fun conditionallyExecuteCleanup(): List<PublicId> =
        if (cleanupConfig.enabled) {
            val maxReportAgeDays = cleanupConfig.maxReportAgeDays?.toLong() ?: Long.MAX_VALUE

            val createdBefore = LocalDate.now().minusDays(maxReportAgeDays)
            val testRunsToCleanUp = testRunRepository.findTestRunsToDelete(createdBefore)

            val cleanedUpTestRunIds = testRunsToCleanUp.mapNotNull {
                conditionallyCleanupTestRun(it, cleanupConfig)
            }

            logger.info("Removed ${cleanedUpTestRunIds.size} created before $createdBefore")

            cleanedUpTestRunIds
        } else {
            logger.info("Clean up not enabled, skipping")

            listOf()
        }

    suspend fun cleanupTestRun(publicId: PublicId) {
        testRunRepository.deleteTestRun(publicId)

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
