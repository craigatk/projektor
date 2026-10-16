package projektor.ai

import io.ktor.server.config.*
import projektor.server.api.config.AIServerConfig
import java.util.*

enum class AIProvider {
    OPENAI,
    ANTHROPIC,
}

data class AIConfig(
    val openAIApiKey: String?,
    val anthropicApiKey: String? = null,
    val anthropicModel: String? = null,
    val configuredProvider: AIProvider? = null,
) {
    /**
     * The provider to use for test failure analysis. If a provider is explicitly configured, it is used
     * only when its API key is also set. Otherwise falls back to whichever provider has an API key,
     * preferring OpenAI for backwards compatibility.
     */
    val provider: AIProvider? =
        if (configuredProvider != null) {
            configuredProvider.takeIf { !apiKeyFor(it).isNullOrBlank() }
        } else {
            AIProvider.entries.firstOrNull { !apiKeyFor(it).isNullOrBlank() }
        }

    private fun apiKeyFor(provider: AIProvider): String? =
        when (provider) {
            AIProvider.OPENAI -> openAIApiKey
            AIProvider.ANTHROPIC -> anthropicApiKey
        }

    companion object {
        fun createAIConfig(applicationConfig: ApplicationConfig): AIConfig {
            val configuredProvider =
                applicationConfig.propertyOrNull("ktor.ai.provider")?.getString()?.trim()?.takeIf { it.isNotEmpty() }?.let {
                    AIProvider.valueOf(it.uppercase())
                }

            return AIConfig(
                openAIApiKey = decodedProperty(applicationConfig, "ktor.ai.openAIApiKey"),
                anthropicApiKey = decodedProperty(applicationConfig, "ktor.ai.anthropicApiKey"),
                anthropicModel =
                    applicationConfig.propertyOrNull(
                        "ktor.ai.anthropicModel",
                    )?.getString()?.trim()?.takeIf { it.isNotEmpty() },
                configuredProvider = configuredProvider,
            )
        }

        private fun decodedProperty(
            applicationConfig: ApplicationConfig,
            propertyName: String,
        ): String? = applicationConfig.propertyOrNull(propertyName)?.getString()?.trim()?.let { base64Decode(it) }?.trim()

        private fun base64Decode(encodedKeyContents: String): String = String(Base64.getDecoder().decode(encodedKeyContents))
    }

    fun toServerConfig(): AIServerConfig = AIServerConfig(testCaseFailureAnalysisEnabled = provider != null)
}
