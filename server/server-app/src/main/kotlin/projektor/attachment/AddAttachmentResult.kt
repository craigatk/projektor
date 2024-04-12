package projektor.attachment

sealed class AddAttachmentResult {
    object Success : AddAttachmentResult()

    class Failure(val errorMessage: String?) : AddAttachmentResult()
}
