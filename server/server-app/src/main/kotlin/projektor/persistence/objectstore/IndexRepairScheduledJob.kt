package projektor.persistence.objectstore

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import projektor.schedule.ScheduleDelay
import projektor.schedule.ScheduledJob
import projektor.schedule.Scheduler
import java.util.concurrent.TimeUnit

/**
 * Rebuilds the index entries of test runs whose index updates failed (see TestRunIndex.repairIndex).
 * Usually there's nothing to repair, which costs a single list request.
 */
class IndexRepairScheduledJob(private val testRunIndex: TestRunIndex) : Runnable {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    override fun run() {
        try {
            runBlocking { testRunIndex.repairIndex() }
        } catch (e: Exception) {
            // Thrown out of a scheduled task, the exception would stop the job from running again
            logger.error("Object store index repair failed", e)
        }
    }

    companion object {
        const val INDEX_REPAIR_JOB_NAME = "repair_object_store_index"

        fun scheduleIndexRepairJob(
            testRunIndex: TestRunIndex,
            scheduler: Scheduler,
        ) {
            scheduler.scheduleJob(
                ScheduledJob(
                    INDEX_REPAIR_JOB_NAME,
                    IndexRepairScheduledJob(testRunIndex),
                    ScheduleDelay(5, TimeUnit.MINUTES),
                ),
            )
        }
    }
}
