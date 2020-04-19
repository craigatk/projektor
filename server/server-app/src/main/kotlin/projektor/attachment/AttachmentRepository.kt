package projektor.attachment

import projektor.server.api.PublicId
import projektor.server.api.attachments.Attachment

interface AttachmentRepository {
    suspend fun addAttachment(publicId: PublicId, attachment: Attachment)

    suspend fun listAttachments(publicId: PublicId): List<Attachment>

    suspend fun deleteAttachment(publicId: PublicId, objectName: String)
}
