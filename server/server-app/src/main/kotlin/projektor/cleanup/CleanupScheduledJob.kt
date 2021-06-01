package projektor.cleanup

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import projektor.schedule.ScheduleDelay
import projektor.schedule.ScheduledJob
import projektor.schedule.Scheduler
import java.util.concurrent.TimeUnit

class CleanupScheduledJob(
    private val testRunCleanupService: TestRunCleanupService,
    private val attachmentCleanupService: AttachmentCleanupService?
) : Runnable {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    override fun run() {
        logger.info("Starting test run cleanup job")

        runBlocking { testRunCleanupService.conditionallyCleanupTestRuns() }

        attachmentCleanupService?.let { runBlocking { it.conditionallyCleanUpAttachments() } }

        logger.info("Competed test run cleanup job.")
    }

    companion object {
        const val cleanupJobName = "cleanup_old_test_runs"

        fun conditionallyStartCleanupScheduledJob(
            cleanupConfig: CleanupConfig,
            testRunCleanupService: TestRunCleanupService,
            attachmentCleanupService: AttachmentCleanupService?,
            scheduler: Scheduler
        ) {
            if (cleanupConfig.enabled) {
                val cleanupScheduledJob = CleanupScheduledJob(testRunCleanupService, attachmentCleanupService)
                val job = ScheduledJob(
                    cleanupJobName,
                    cleanupScheduledJob,
                    ScheduleDelay(1, TimeUnit.DAYS)
                )

                scheduler.scheduleJob(job)
            }
        }
    }
}
