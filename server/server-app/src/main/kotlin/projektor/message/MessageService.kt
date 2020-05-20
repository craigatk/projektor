package projektor.message

import io.ktor.util.KtorExperimentalAPI
import projektor.server.api.PublicId
import projektor.server.api.messages.Messages

@KtorExperimentalAPI
class MessageService(private val messageConfig: MessageConfig) {

    fun getTestRunMessages(publicId: PublicId): Messages {
        return Messages(messageConfig.globalMessages)
    }
}
