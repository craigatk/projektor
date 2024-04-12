package projektor.server.api.attachments

data class AddAttachmentResponse(
    val name: String?,
    val error: AddAttachmentError?,
)

enum class AddAttachmentError {
    ATTACHMENTS_DISABLED,
    ATTACHMENT_TOO_LARGE,
    ATTACHMENT_FAILED,
}
