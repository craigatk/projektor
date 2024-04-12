package projektor.message

import projektor.server.api.PublicId
import projektor.server.api.messages.Messages

class MessageService(private val messageConfig: MessageConfig) {
    fun getTestRunMessages(publicId: PublicId): Messages {
        return Messages(messageConfig.globalMessages)
    }
}
