package projektor.message

import io.ktor.config.ApplicationConfig

data class MessageConfig(val globalMessages: List<String>) {

    companion object {
        fun createMessageConfig(applicationConfig: ApplicationConfig): MessageConfig {
            val messageString = applicationConfig.propertyOrNull("ktor.message.global")?.getString()
            val messages = messageString?.split("|") ?: listOf()

            return MessageConfig(messages)
        }
    }
}
