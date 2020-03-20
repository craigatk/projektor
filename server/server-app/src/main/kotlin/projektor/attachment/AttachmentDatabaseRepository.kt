package projektor.attachment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import projektor.database.generated.Tables
import projektor.database.generated.tables.daos.TestRunAttachmentDao
import projektor.server.api.PublicId
import projektor.server.api.attachments.Attachment

class AttachmentDatabaseRepository(private val dslContext: DSLContext) : AttachmentRepository {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    override suspend fun addAttachment(publicId: PublicId, attachment: Attachment) {
        withContext(Dispatchers.IO) {
            dslContext.transaction { configuration ->
                val attachmentDao = TestRunAttachmentDao(configuration)

                attachmentDao.insert(attachment.toDB(publicId))
            }
        }
    }

    override suspend fun listAttachments(publicId: PublicId): List<Attachment> =
            withContext(Dispatchers.IO) {
                dslContext.select(Tables.TEST_RUN_ATTACHMENT.fields().toList())
                        .from(Tables.TEST_RUN_ATTACHMENT)
                        .where(Tables.TEST_RUN_ATTACHMENT.TEST_RUN_PUBLIC_ID.eq(publicId.id))
                        .fetchInto(Attachment::class.java)
            }
}
