package projektor.schedule

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Scheduler(private val schedulerLock: SchedulerLock) {
    private val executor = Executors.newScheduledThreadPool(1)

    private val scheduledJobs = ConcurrentHashMap<String, ScheduledJob>()

    fun scheduleJob(job: ScheduledJob) {
        val runnableWithLock = Runnable {
            schedulerLock.executeWithLock(job.runnable, SchedulerLockConfig(60, job.name))
        }

        executor.scheduleWithFixedDelay(runnableWithLock, job.scheduleDelay.value, job.scheduleDelay.value, job.scheduleDelay.unit)

        scheduledJobs[job.name] = job
    }

    fun findScheduledJob(jobName: String) = scheduledJobs[jobName]
}

data class ScheduleDelay(val value: Long, val unit: TimeUnit)

data class ScheduledJob(val name: String, val runnable: Runnable, val scheduleDelay: ScheduleDelay)
