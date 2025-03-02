package projektor.ai

import io.ktor.server.config.*
import projektor.server.api.config.AIServerConfig
import java.util.*

data class AIConfig(val openAIApiKey: String?) {
    companion object {
        fun createAIConfig(applicationConfig: ApplicationConfig): AIConfig {
            val encodedOpenAIApiKey: String? =
                applicationConfig.propertyOrNull(
                    "ktor.ai.openAIApiKey",
                )?.getString()?.trim()

            val decodedOpenAIApiKey = encodedOpenAIApiKey?.let { base64Decode(it) }?.trim()

            return AIConfig(decodedOpenAIApiKey)
        }

        private fun base64Decode(encodedKeyContents: String): String = String(Base64.getDecoder().decode(encodedKeyContents))
    }

    fun toServerConfig(): AIServerConfig = AIServerConfig(testCaseFailureAnalysisEnabled = !openAIApiKey.isNullOrBlank())
}
