package projektor.server.api.attachments

data class Attachment(val fileName: String, val objectName: String, val fileSize: Long?) {
    val attachmentType: AttachmentType
        get() =
            when {
                fileName.endsWith(".png") || fileName.endsWith(".jpg") -> AttachmentType.IMAGE
                fileName.endsWith(".mp4") -> AttachmentType.VIDEO
                else -> AttachmentType.OTHER
            }
}
