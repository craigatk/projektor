package projektor.attachment

sealed class AddAttachmentResult {
    object Success : AddAttachmentResult()

    object TooLarge : AddAttachmentResult()

    class Failure(val errorMessage: String?) : AddAttachmentResult()
}
