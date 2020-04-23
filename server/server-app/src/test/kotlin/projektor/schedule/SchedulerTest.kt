package projektor.schedule

import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isNotNull

@ExperimentalTime
class SchedulerTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should run scheduled job`() {
        var schedulerCalled = false

        val scheduledJob = Runnable {
            schedulerCalled = true
        }

        val schedulerLock = SchedulerLock(dataSource)
        val scheduler = Scheduler(schedulerLock)

        val scheduleDuration = measureTime {
            scheduler.scheduleJob(ScheduledJob("my_job", scheduledJob, ScheduleDelay(4, TimeUnit.SECONDS)))

            await until { schedulerCalled }
        }

        expectThat(scheduleDuration.inSeconds).isGreaterThanOrEqualTo(4.toDouble())

        expectThat(scheduler.findScheduledJob("my_job")).isNotNull().and {
            get { name }.isEqualTo("my_job")
            get { scheduleDelay }.isEqualTo(ScheduleDelay(4, TimeUnit.SECONDS))
        }
    }
}
