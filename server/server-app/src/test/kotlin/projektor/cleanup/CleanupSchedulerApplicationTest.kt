package projektor.cleanup

import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Test
import org.koin.ktor.ext.get
import projektor.ApplicationTestCase
import projektor.schedule.ScheduleDelay
import projektor.schedule.Scheduler
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.util.concurrent.TimeUnit

@KtorExperimentalAPI
class CleanupSchedulerApplicationTest : ApplicationTestCase() {
    @Test
    fun `when cleanup enabled should schedule cleanup job`() {
        cleanupMaxAgeDays = 30

        withTestApplication(::createTestApplication) {
            val scheduler: Scheduler = application.get()

            val cleanupJob = scheduler.findScheduledJob(CleanupScheduledJob.cleanupJobName)
            expectThat(cleanupJob)
                .isNotNull()
                .and {
                    get { scheduleDelay }.isEqualTo(ScheduleDelay(1, TimeUnit.DAYS))
                }
        }
    }

    @Test
    fun `when cleanup not enabled should not schedule cleanup job`() {
        cleanupMaxAgeDays = null

        withTestApplication(::createTestApplication) {
            val scheduler: Scheduler = application.get()

            val cleanupJob = scheduler.findScheduledJob(CleanupScheduledJob.cleanupJobName)
            expectThat(cleanupJob).isNull()
        }
    }
}
