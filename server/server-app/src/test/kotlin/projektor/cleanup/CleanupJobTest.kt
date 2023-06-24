package projektor.cleanup

import org.junit.jupiter.api.Test
import org.koin.test.get
import projektor.DatabaseRepositoryTestCase
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.time.LocalDate

class CleanupJobTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should execute test report cleanup`() {
        val cleanupService = TestRunCleanupService(
            CleanupConfig(30, null, false),
            get(),
            get(),
            get(),
            null
        )
        val cleanupScheduledJob = CleanupScheduledJob(cleanupService, null)

        val publicIdToDelete = randomPublicId()
        val testRunToDelete = testRunDBGenerator.createTestRun(publicIdToDelete, LocalDate.now().minusDays(31), false)

        val publicIdToSave = randomPublicId()
        val testRunToSave = testRunDBGenerator.createTestRun(publicIdToSave, LocalDate.now().minusDays(29), false)

        cleanupScheduledJob.run()

        expectThat(testRunDao.fetchOneById(testRunToDelete.id)).isNull()
        expectThat(testRunDao.fetchOneById(testRunToSave.id)).isNotNull()
    }
}
