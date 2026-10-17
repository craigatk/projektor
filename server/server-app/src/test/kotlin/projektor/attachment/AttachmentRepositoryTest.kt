package projektor.attachment

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.incomingresults.randomPublicId
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.get
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

abstract class AttachmentRepositoryTest : RepositoryTestCase() {
    @Test
    fun `when test run has two attachments should return them`() {
        val publicId = randomPublicId()

        testRunGenerator.addAttachment(publicId, "object1", "file1.txt")
        testRunGenerator.addAttachment(publicId, "object2", "file2.txt")

        val anotherPublicId = randomPublicId()
        testRunGenerator.addAttachment(anotherPublicId, "other", "other.txt")

        val attachments = runBlocking { attachmentRepository.listAttachments(publicId) }
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
        val somePublicId = randomPublicId()
        val attachments = runBlocking { attachmentRepository.listAttachments(somePublicId) }
        expectThat(attachments).hasSize(0)
    }

    @Test
    fun `when test run has attachments should delete them`() {
        val publicId = randomPublicId()

        testRunGenerator.addAttachment(publicId, "shouldNotDelete", "file1.txt")
        testRunGenerator.addAttachment(publicId, "shouldDelete", "file2.txt")

        val anotherPublicId = randomPublicId()
        testRunGenerator.addAttachment(anotherPublicId, "shouldDelete", "other.txt")

        runBlocking { attachmentRepository.deleteAttachment(publicId, "shouldDelete") }

        expectThat(runBlocking { attachmentRepository.listAttachments(publicId) })
            .hasSize(1)
            .and {
                get(0).get { objectName }.isEqualTo("shouldNotDelete")
            }

        expectThat(runBlocking { attachmentRepository.listAttachments(anotherPublicId) })
            .hasSize(1)
            .and {
                get(0).get { objectName }.isEqualTo("shouldDelete")
            }
    }
}

class AttachmentRepositoryDatabaseTest : AttachmentRepositoryTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class AttachmentRepositoryObjectStoreTest : AttachmentRepositoryTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
