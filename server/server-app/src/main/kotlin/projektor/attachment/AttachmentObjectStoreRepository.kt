package projektor.attachment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import projektor.objectstore.json.JsonObjectStore
import projektor.persistence.objectstore.AttachmentDocument
import projektor.persistence.objectstore.AttachmentsDocument
import projektor.persistence.objectstore.ObjectStoreKeys
import projektor.server.api.PublicId
import projektor.server.api.attachments.Attachment

class AttachmentObjectStoreRepository(private val store: JsonObjectStore) : AttachmentRepository {
    override suspend fun addAttachment(
        publicId: PublicId,
        attachment: Attachment,
    ) {
        withContext(Dispatchers.IO) {
            store.update<AttachmentsDocument>(ObjectStoreKeys.attachments(publicId)) { current ->
                AttachmentsDocument(
                    current?.attachments.orEmpty() + AttachmentDocument(attachment.fileName, attachment.objectName, attachment.fileSize),
                )
            }
        }
    }

    override suspend fun listAttachments(publicId: PublicId): List<Attachment> =
        withContext(Dispatchers.IO) {
            store.read<AttachmentsDocument>(ObjectStoreKeys.attachments(publicId))
                ?.attachments
                ?.map { Attachment(it.fileName, it.objectName, it.fileSize) }
                ?: listOf()
        }

    override suspend fun deleteAttachment(
        publicId: PublicId,
        objectName: String,
    ) {
        withContext(Dispatchers.IO) {
            // The document is left in place even when empty, deleting it could lose an attachment added concurrently
            store.update<AttachmentsDocument>(ObjectStoreKeys.attachments(publicId)) { current ->
                current?.let { AttachmentsDocument(it.attachments.filter { attachment -> attachment.objectName != objectName }) }
            }
        }
    }
}
