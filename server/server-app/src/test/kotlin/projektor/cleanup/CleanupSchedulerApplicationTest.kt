package projektor.cleanup

import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Test
import org.koin.ktor.ext.get
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.schedule.ScheduleDelay
import projektor.schedule.Scheduler
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.test.assertNotNull

@KtorExperimentalAPI
class CleanupSchedulerApplicationTest : ApplicationTestCase() {
    @Test
    fun `when report cleanup enabled should schedule cleanup job`() {
        reportCleanupMaxAgeDays = 30

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
    fun `when attachment cleanup enabled should run attachment cleanup`() {
        attachmentsEnabled = true
        attachmentCleanupMaxAgeDays = 14

        withTestApplication(::createTestApplication) {
            val cleanupPublicId = randomPublicId()
            testRunDBGenerator.createTestRun(cleanupPublicId, LocalDate.now().minusDays(16), false)
            testRunDBGenerator.addAttachment(cleanupPublicId, "attachment", "attachment.txt")

            val tooNewPublicId = randomPublicId()
            testRunDBGenerator.createTestRun(tooNewPublicId, LocalDate.now().minusDays(2), false)
            testRunDBGenerator.addAttachment(tooNewPublicId, "attachment", "attachment.txt")

            val scheduler: Scheduler = application.get()

            val cleanupJob = scheduler.findScheduledJob(CleanupScheduledJob.cleanupJobName)
            assertNotNull(cleanupJob)

            cleanupJob.runnable.run()

            val attachmentsAfterCleanup = attachmentDao.fetchByTestRunPublicId(cleanupPublicId.id)
            expectThat(attachmentsAfterCleanup).hasSize(0)

            val tooNewAttachmentsAfterCleanup = attachmentDao.fetchByTestRunPublicId(tooNewPublicId.id)
            expectThat(tooNewAttachmentsAfterCleanup).hasSize(1)
        }
    }

    @Test
    fun `when cleanup not enabled should not schedule cleanup job`() {
        reportCleanupMaxAgeDays = null

        withTestApplication(::createTestApplication) {
            val scheduler: Scheduler = application.get()

            val cleanupJob = scheduler.findScheduledJob(CleanupScheduledJob.cleanupJobName)
            expectThat(cleanupJob).isNull()
        }
    }
}
