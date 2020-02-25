package projektor.attachment

import projektor.database.generated.tables.pojos.TestRunAttachment

fun Attachment.toDB(testRunId: Long): TestRunAttachment {
    val attachmentDB = TestRunAttachment()
    attachmentDB.testRunId = testRunId
    attachmentDB.fileName = fileName
    attachmentDB.objectName = objectName
    attachmentDB.fileSize = fileSize

    return attachmentDB
}
