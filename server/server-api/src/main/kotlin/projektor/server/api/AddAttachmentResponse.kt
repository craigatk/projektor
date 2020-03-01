package projektor.server.api

data class AddAttachmentResponse(
    val successful: Boolean,
    val name: String?,
    val error: AddAttachmentError?
)

enum class AddAttachmentError {
    ATTACHMENTS_DISABLED,
    ATTACHMENT_TOO_LARGE
}
