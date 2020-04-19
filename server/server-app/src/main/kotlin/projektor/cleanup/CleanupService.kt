package projektor.cleanup

import io.ktor.util.KtorExperimentalAPI
import projektor.attachment.AttachmentService
import projektor.server.api.PublicId
import projektor.testrun.TestRunRepository

@KtorExperimentalAPI
class CleanupService(private val testRunRepository: TestRunRepository, private val attachmentService: AttachmentService?) {
    suspend fun cleanupTestRun(publicId: PublicId) {
        testRunRepository.deleteTestRun(publicId)

        attachmentService?.deleteAttachments(publicId)
    }
}
