package projektor.attachment

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.database.generated.tables.pojos.TestRunAttachment
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.get
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class AttachmentDatabaseRepositoryTest : DatabaseRepositoryTestCase() {

    @Test
    fun `when test run has two attachments should return them`() {
        val publicId = randomPublicId()
        val attachmentDatabaseRepository = AttachmentDatabaseRepository(dslContext)

        val attachment1 = TestRunAttachment()
        attachment1.testRunPublicId = publicId.id
        attachment1.objectName = "object1"
        attachment1.fileName = "file1.txt"
        attachmentDao.insert(attachment1)

        val attachment2 = TestRunAttachment()
        attachment2.testRunPublicId = publicId.id
        attachment2.objectName = "object2"
        attachment2.fileName = "file2.txt"
        attachmentDao.insert(attachment2)

        val anotherPublicId = randomPublicId()
        val attachmentForAnotherTestRun = TestRunAttachment()
        attachmentForAnotherTestRun.testRunPublicId = anotherPublicId.id
        attachmentForAnotherTestRun.objectName = "other"
        attachmentForAnotherTestRun.fileName = "other.txt"
        attachmentDao.insert(attachmentForAnotherTestRun)

        val attachments = runBlocking { attachmentDatabaseRepository.listAttachments(publicId) }
        expectThat(attachments).hasSize(2).and {
            any {
                get { objectName }.isEqualTo("object1")
            }
            any {
                get { objectName }.isEqualTo("object2")
            }
        }
    }

    @Test
    fun `when test run has no attachments should return empty list`() {
        val attachmentDatabaseRepository = AttachmentDatabaseRepository(dslContext)

        val somePublicId = randomPublicId()
        val attachments = runBlocking { attachmentDatabaseRepository.listAttachments(somePublicId) }
        expectThat(attachments).hasSize(0)
    }

    @Test
    fun `when test run has attachments should delete them`() {
        val publicId = randomPublicId()
        val attachmentDatabaseRepository = AttachmentDatabaseRepository(dslContext)

        val attachment1 = TestRunAttachment()
        attachment1.testRunPublicId = publicId.id
        attachment1.objectName = "shouldNotDelete"
        attachment1.fileName = "file1.txt"
        attachmentDao.insert(attachment1)

        val attachment2 = TestRunAttachment()
        attachment2.testRunPublicId = publicId.id
        attachment2.objectName = "shouldDelete"
        attachment2.fileName = "file2.txt"
        attachmentDao.insert(attachment2)

        val anotherPublicId = randomPublicId()
        val attachmentForAnotherTestRun = TestRunAttachment()
        attachmentForAnotherTestRun.testRunPublicId = anotherPublicId.id
        attachmentForAnotherTestRun.objectName = "shouldDelete"
        attachmentForAnotherTestRun.fileName = "other.txt"
        attachmentDao.insert(attachmentForAnotherTestRun)

        runBlocking { attachmentDatabaseRepository.deleteAttachment(publicId, "shouldDelete") }

        expectThat(attachmentDao.fetchByTestRunPublicId(publicId.id))
                .hasSize(1)
                .and {
                    get(0).get { objectName }.isEqualTo("shouldNotDelete")
                }

        expectThat(attachmentDao.fetchByTestRunPublicId(anotherPublicId.id))
                .hasSize(1)
                .and {
                    get(0).get { objectName }.isEqualTo("shouldDelete")
                }
    }
}
