package projektor.attachment

import projektor.database.generated.tables.pojos.TestRunAttachment
import projektor.server.api.PublicId
import projektor.server.api.attachments.Attachment

fun Attachment.toDB(publicId: PublicId): TestRunAttachment {
    val attachmentDB = TestRunAttachment()
    attachmentDB.testRunPublicId = publicId.id
    attachmentDB.fileName = fileName
    attachmentDB.objectName = objectName
    attachmentDB.fileSize = fileSize

    return attachmentDB
}
