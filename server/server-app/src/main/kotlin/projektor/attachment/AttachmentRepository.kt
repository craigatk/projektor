package projektor.attachment

import projektor.server.api.Attachment
import projektor.server.api.PublicId

interface AttachmentRepository {
    suspend fun addAttachment(publicId: PublicId, attachment: Attachment)

    suspend fun listAttachments(publicId: PublicId): List<Attachment>
}
