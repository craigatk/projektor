package projektor.cleanup

import org.slf4j.LoggerFactory
import projektor.attachment.AttachmentService
import projektor.server.api.PublicId
import projektor.testrun.TestRunRepository
import java.time.LocalDate

class AttachmentCleanupService(
    private val cleanupConfig: CleanupConfig,
    private val testRunRepository: TestRunRepository,
    private val attachmentService: AttachmentService
) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    suspend fun conditionallyCleanUpAttachments(): List<PublicId> =
        if (cleanupConfig.attachmentCleanupEnabled) {
            val maxAttachmentAgeDays = cleanupConfig.maxAttachmentAgeDays?.toLong() ?: 0

            val createdBefore = LocalDate.now().minusDays(maxAttachmentAgeDays)
            val testRunsWithAttachmentsToDelete = testRunRepository.findTestRunsCreatedBeforeAndNotPinnedWithAttachments(createdBefore)

            val testRunsWithAttachmentsDeleted = testRunsWithAttachmentsToDelete.mapNotNull {
                conditionallyRemoveAttachments(it, cleanupConfig)
            }

            logger.info("Removed attachments from ${testRunsWithAttachmentsDeleted.size} test runs created before $createdBefore")

            testRunsWithAttachmentsDeleted
        } else {
            logger.info("Additional attachments clean up not enabled, skipping")

            listOf()
        }

    private suspend fun conditionallyRemoveAttachments(publicId: PublicId, cleanupConfig: CleanupConfig): PublicId? =
        try {
            if (!cleanupConfig.dryRun) {
                attachmentService.deleteAttachments(publicId)
            } else {
                logger.info("Not deleting attachments from test run $publicId since dry run is enabled")
            }
            publicId
        } catch (e: Exception) {
            logger.error("Failed to delete attachments from test run $publicId", e)
            null
        }
}
