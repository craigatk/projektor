package projektor.attachment

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
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

        testRunDBGenerator.addAttachment(publicId, "object1", "file1.txt")
        testRunDBGenerator.addAttachment(publicId, "object2", "file2.txt")

        val anotherPublicId = randomPublicId()
        testRunDBGenerator.addAttachment(anotherPublicId, "other", "other.txt")

        val attachments = runBlocking { attachmentDatabaseRepository.listAttachments(publicId) }
        expectThat(attachments).hasSize(2).and {
            any {
                get { objectName }.isEqualTo("object1")
                get { fileName }.isEqualTo("file1.txt")
            }
            any {
                get { objectName }.isEqualTo("object2")
                get { fileName }.isEqualTo("file2.txt")
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

        testRunDBGenerator.addAttachment(publicId, "shouldNotDelete", "file1.txt")
        testRunDBGenerator.addAttachment(publicId, "shouldDelete", "file2.txt")

        val anotherPublicId = randomPublicId()
        testRunDBGenerator.addAttachment(anotherPublicId, "shouldDelete", "other.txt")

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
