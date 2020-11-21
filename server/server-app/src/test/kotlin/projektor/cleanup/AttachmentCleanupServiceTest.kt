package projektor.cleanup

import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.test.get
import projektor.DatabaseRepositoryTestCase
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.doesNotContain
import strikt.assertions.hasSize
import strikt.assertions.isNotNull
import java.time.LocalDate

@KtorExperimentalAPI
class AttachmentCleanupServiceTest : DatabaseRepositoryTestCase() {

    init {
        attachmentsEnabled = true
    }

    @Test
    fun `should delete attachments from test runs older than specified day`() {
        val attachmentCleanupService = AttachmentCleanupService(
            CleanupConfig(null, 21, false),
            get(),
            attachmentService
        )

        val cleanupPublicId = randomPublicId()
        testRunDBGenerator.createTestRun(cleanupPublicId, LocalDate.now().minusDays(23), false)
        testRunDBGenerator.addAttachment(cleanupPublicId, "attachment", "attachment.txt")

        val tooNewPublicId = randomPublicId()
        testRunDBGenerator.createTestRun(tooNewPublicId, LocalDate.now().minusDays(2), false)
        testRunDBGenerator.addAttachment(tooNewPublicId, "attachment", "attachment.txt")

        val cleanedUpPublicIds = runBlocking { attachmentCleanupService.conditionallyCleanUpAttachments() }
        expectThat(cleanedUpPublicIds)
            .contains(cleanupPublicId)
            .doesNotContain(tooNewPublicId)

        val attachmentsAfterCleanup = attachmentDao.fetchByTestRunPublicId(cleanupPublicId.id)
        expectThat(attachmentsAfterCleanup).hasSize(0)

        val testRunAfterCleanup = testRunDao.fetchOneByPublicId(cleanupPublicId.id)
        expectThat(testRunAfterCleanup).isNotNull()

        val tooNewAttachmentsAfterCleanup = attachmentDao.fetchByTestRunPublicId(tooNewPublicId.id)
        expectThat(tooNewAttachmentsAfterCleanup).hasSize(1)
    }

    @Test
    fun `when dry run enabled should not delete any attachments`() {
        val attachmentCleanupService = AttachmentCleanupService(
            CleanupConfig(null, 14, true),
            get(),
            attachmentService
        )

        val cleanupPublicId = randomPublicId()
        testRunDBGenerator.createTestRun(cleanupPublicId, LocalDate.now().minusDays(16), false)
        testRunDBGenerator.addAttachment(cleanupPublicId, "attachment", "attachment.txt")

        runBlocking { attachmentCleanupService.conditionallyCleanUpAttachments() }

        val attachmentsAfterCleanup = attachmentDao.fetchByTestRunPublicId(cleanupPublicId.id)
        expectThat(attachmentsAfterCleanup).hasSize(1)

        val testRunAfterCleanup = testRunDao.fetchOneByPublicId(cleanupPublicId.id)
        expectThat(testRunAfterCleanup).isNotNull()
    }
}
