package projektor.schedule

import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Scheduler(private val schedulerLock: SchedulerLock) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    private val executor = Executors.newScheduledThreadPool(1)

    private val scheduledJobs = ConcurrentHashMap<String, ScheduledJob>()

    fun scheduleJob(job: ScheduledJob) {
        val runnableWithLock =
            Runnable {
                // An exception escaping a task scheduled with fixed delay cancels all its later runs. This also covers
                // taking and releasing the lock, which happen outside the job's own code.
                try {
                    schedulerLock.executeWithLock(job.runnable, SchedulerLockConfig(60, job.name))
                } catch (e: Throwable) {
                    logger.error("Scheduled job ${job.name} failed, it will run again after its next delay", e)
                }
            }

        executor.scheduleWithFixedDelay(runnableWithLock, job.scheduleDelay.value, job.scheduleDelay.value, job.scheduleDelay.unit)

        scheduledJobs[job.name] = job
    }

    fun findScheduledJob(jobName: String) = scheduledJobs[jobName]
}

data class ScheduleDelay(val value: Long, val unit: TimeUnit)

data class ScheduledJob(val name: String, val runnable: Runnable, val scheduleDelay: ScheduleDelay)
