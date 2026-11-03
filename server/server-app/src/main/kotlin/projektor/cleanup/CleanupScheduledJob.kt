package projektor.cleanup

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import projektor.schedule.ScheduleDelay
import projektor.schedule.ScheduledJob
import projektor.schedule.Scheduler
import java.util.concurrent.TimeUnit

class CleanupScheduledJob(
    private val testRunCleanupService: TestRunCleanupService,
    private val attachmentCleanupService: AttachmentCleanupService?,
) : Runnable {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    override fun run() {
        logger.info("Starting test run cleanup job")

        // Separately, so failing to find test runs to clean up doesn't also stop attachments from being cleaned up
        try {
            runBlocking { testRunCleanupService.conditionallyCleanupTestRuns() }
        } catch (e: Exception) {
            logger.error("Test run cleanup failed, it will run again tomorrow", e)
        }

        try {
            attachmentCleanupService?.let { runBlocking { it.conditionallyCleanUpAttachments() } }
        } catch (e: Exception) {
            logger.error("Attachment cleanup failed, it will run again tomorrow", e)
        }

        logger.info("Competed test run cleanup job.")
    }

    companion object {
        const val CLEANUP_JOB_NAME = "cleanup_old_test_runs"

        fun conditionallyStartCleanupScheduledJob(
            cleanupConfig: CleanupConfig,
            testRunCleanupService: TestRunCleanupService,
            attachmentCleanupService: AttachmentCleanupService?,
            scheduler: Scheduler,
        ) {
            if (cleanupConfig.enabled) {
                val cleanupScheduledJob = CleanupScheduledJob(testRunCleanupService, attachmentCleanupService)
                val job =
                    ScheduledJob(
                        CLEANUP_JOB_NAME,
                        cleanupScheduledJob,
                        ScheduleDelay(1, TimeUnit.DAYS),
                        // Soon after startup, since a server restarted more often than daily would otherwise never run it
                        initialDelay = ScheduleDelay(10, TimeUnit.MINUTES),
                    )

                scheduler.scheduleJob(job)
            }
        }
    }
}
