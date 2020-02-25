package projektor.attachment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import projektor.database.generated.Tables
import projektor.database.generated.tables.daos.TestRunAttachmentDao
import projektor.server.api.PublicId

class AttachmentDatabaseRepository(private val dslContext: DSLContext) : AttachmentRepository {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    override suspend fun addAttachment(publicId: PublicId, attachment: Attachment) {
        withContext(Dispatchers.IO) {
            dslContext.transaction { configuration ->
                val testRunId = dslContext.select(Tables.TEST_RUN.ID)
                        .from(Tables.TEST_RUN)
                        .where(Tables.TEST_RUN.PUBLIC_ID.eq(publicId.id))
                        .fetchOne(Tables.TEST_RUN.ID)

                val attachmentDao = TestRunAttachmentDao(configuration)

                attachmentDao.insert(attachment.toDB(testRunId))

                dslContext.update(Tables.TEST_RUN)
                        .set(Tables.TEST_RUN.HAS_ATTACHMENTS, true)
                        .where(Tables.TEST_RUN.ID.eq(testRunId))
                        .execute()
            }
        }
    }

    override suspend fun listAttachments(publicId: PublicId): List<Attachment> =
            withContext(Dispatchers.IO) {
                dslContext.select(Tables.TEST_RUN_ATTACHMENT.fields().toList())
                        .from(Tables.TEST_RUN_ATTACHMENT)
                        .innerJoin(Tables.TEST_RUN).on(Tables.TEST_RUN_ATTACHMENT.TEST_RUN_ID.eq(Tables.TEST_RUN.ID))
                        .where(Tables.TEST_RUN.PUBLIC_ID.eq(publicId.id))
                        .fetchInto(Attachment::class.java)
            }
}
