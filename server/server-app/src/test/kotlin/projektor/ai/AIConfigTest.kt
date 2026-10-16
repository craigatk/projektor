package projektor.ai

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class AIConfigTest {
    @Test
    fun `should use explicitly configured provider when its key is set`() {
        val aiConfig = AIConfig(openAIApiKey = "openai", anthropicApiKey = "anthropic", configuredProvider = AIProvider.ANTHROPIC)

        expectThat(aiConfig.provider).isEqualTo(AIProvider.ANTHROPIC)
    }

    @Test
    fun `should not fall back to another provider when configured provider has no key`() {
        val aiConfig = AIConfig(openAIApiKey = "openai", anthropicApiKey = null, configuredProvider = AIProvider.ANTHROPIC)

        expectThat(aiConfig.provider).isNull()
    }

    @Test
    fun `should prefer OpenAI when no provider is configured and both keys are set`() {
        val aiConfig = AIConfig(openAIApiKey = "openai", anthropicApiKey = "anthropic")

        expectThat(aiConfig.provider).isEqualTo(AIProvider.OPENAI)
    }

    @Test
    fun `should use Anthropic when no provider is configured and only its key is set`() {
        val aiConfig = AIConfig(openAIApiKey = null, anthropicApiKey = "anthropic")

        expectThat(aiConfig.provider).isEqualTo(AIProvider.ANTHROPIC)
    }

    @Test
    fun `should have no provider when no keys are set`() {
        val aiConfig = AIConfig(openAIApiKey = null)

        expectThat(aiConfig.provider).isNull()
    }
}
