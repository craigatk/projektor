package projektor.schedule

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.core.SimpleLock
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class SchedulerFailureTest {
    private val unlockedLock = SimpleLock { }

    private fun fastJob(
        name: String,
        runnable: Runnable,
    ) = ScheduledJob(name, runnable, ScheduleDelay(50, TimeUnit.MILLISECONDS))

    @Test
    fun `should first run a job after its initial delay rather than its schedule delay`() {
        val jobRuns = AtomicInteger()

        Scheduler(SchedulerLock { Optional.of(unlockedLock) }).scheduleJob(
            ScheduledJob(
                "initial_delay",
                { jobRuns.incrementAndGet() },
                ScheduleDelay(1, TimeUnit.DAYS),
                initialDelay = ScheduleDelay(50, TimeUnit.MILLISECONDS),
            ),
        )

        await until { jobRuns.get() == 1 }
    }

    @Test
    fun `should keep running a job after failing to take its lock`() {
        val lockAttempts = AtomicInteger()
        val jobRuns = AtomicInteger()

        val lockProvider =
            LockProvider {
                if (lockAttempts.incrementAndGet() == 1) throw IllegalStateException("Simulated store failure")
                Optional.of(unlockedLock)
            }

        Scheduler(SchedulerLock(lockProvider)).scheduleJob(fastJob("lock_failure", { jobRuns.incrementAndGet() }))

        await until { jobRuns.get() >= 2 }
    }

    @Test
    fun `should keep running a job after failing to release its lock`() {
        val jobRuns = AtomicInteger()
        val unlockAttempts = AtomicInteger()

        val lockProvider =
            LockProvider {
                Optional.of(
                    SimpleLock {
                        if (unlockAttempts.incrementAndGet() == 1) throw IllegalStateException("Simulated store failure")
                    },
                )
            }

        Scheduler(SchedulerLock(lockProvider)).scheduleJob(fastJob("unlock_failure", { jobRuns.incrementAndGet() }))

        await until { jobRuns.get() >= 2 }
    }

    @Test
    fun `should keep running a job after it throws`() {
        val jobRuns = AtomicInteger()

        val job =
            Runnable {
                if (jobRuns.incrementAndGet() == 1) throw IllegalStateException("Simulated job failure")
            }

        Scheduler(SchedulerLock(LockProvider { Optional.of(unlockedLock) })).scheduleJob(fastJob("job_failure", job))

        await until { jobRuns.get() >= 2 }
    }
}
