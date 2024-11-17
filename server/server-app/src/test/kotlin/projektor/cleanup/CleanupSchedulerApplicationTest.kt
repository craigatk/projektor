package projektor.cleanup

import org.junit.jupiter.api.Test
import org.koin.ktor.ext.get
import projektor.ApplicationTestCase
import projektor.ApplicationTestCaseConfig
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

class CleanupSchedulerApplicationTest : ApplicationTestCase() {
    @Test
    fun `when report cleanup enabled should schedule cleanup job`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                reportCleanupMaxAgeDays = 30,
            ),
        ) {
            val scheduler: Scheduler = getApplication().get()

            val cleanupJob = scheduler.findScheduledJob(CleanupScheduledJob.CLEANUP_JOB_NAME)
            expectThat(cleanupJob)
                .isNotNull()
                .and {
                    get { scheduleDelay }.isEqualTo(ScheduleDelay(1, TimeUnit.DAYS))
                }
        }

    @Test
    fun `when attachment cleanup enabled should run attachment cleanup`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                attachmentsEnabled = true,
                attachmentCleanupMaxAgeDays = 14,
            ),
        ) {
            val cleanupPublicId = randomPublicId()
            testRunDBGenerator.createTestRun(cleanupPublicId, LocalDate.now().minusDays(16), false)
            testRunDBGenerator.addAttachment(cleanupPublicId, "attachment", "attachment.txt")

            val tooNewPublicId = randomPublicId()
            testRunDBGenerator.createTestRun(tooNewPublicId, LocalDate.now().minusDays(2), false)
            testRunDBGenerator.addAttachment(tooNewPublicId, "attachment", "attachment.txt")

            val scheduler: Scheduler = getApplication().get()

            val cleanupJob = scheduler.findScheduledJob(CleanupScheduledJob.CLEANUP_JOB_NAME)
            assertNotNull(cleanupJob)

            cleanupJob.runnable.run()

            val attachmentsAfterCleanup = attachmentDao.fetchByTestRunPublicId(cleanupPublicId.id)
            expectThat(attachmentsAfterCleanup).hasSize(0)

            val tooNewAttachmentsAfterCleanup = attachmentDao.fetchByTestRunPublicId(tooNewPublicId.id)
            expectThat(tooNewAttachmentsAfterCleanup).hasSize(1)
        }

    @Test
    fun `when cleanup not enabled should not schedule cleanup job`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                reportCleanupMaxAgeDays = null,
            ),
        ) {
            val scheduler: Scheduler = getApplication().get()

            val cleanupJob = scheduler.findScheduledJob(CleanupScheduledJob.CLEANUP_JOB_NAME)
            expectThat(cleanupJob).isNull()
        }
}
