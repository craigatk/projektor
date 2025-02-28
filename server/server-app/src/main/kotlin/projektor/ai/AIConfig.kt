package projektor.ai

import io.ktor.server.config.*

data class AIConfig(val openAIApiKey: String?) {
    companion object {
        fun createAIConfig(applicationConfig: ApplicationConfig): AIConfig {
            val openAIApiKey: String? =
                applicationConfig.propertyOrNull(
                    "ktor.ai.openAIApiKey",
                )?.toString()

            return AIConfig(openAIApiKey)
        }
    }
}
