package projektor.ai.anthropic

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.core.JsonValue
import com.anthropic.models.beta.messages.BetaStopReason
import com.anthropic.models.beta.messages.MessageCreateParams
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import projektor.ai.analysis.AITestFailureAnalyzer
import projektor.ai.analysis.TestFailureAnalysis

class AnthropicAnalysisClient(
    apiKey: String,
    baseUrl: String? = null,
    private val model: String = DEFAULT_MODEL,
) : AITestFailureAnalyzer {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    private val client: AnthropicClient =
        AnthropicOkHttpClient.builder()
            .apiKey(apiKey)
            .apply { baseUrl?.let { baseUrl(it) } }
            .build()

    override suspend fun analyzeTestFailure(testOutput: String): TestFailureAnalysis? {
        val params =
            MessageCreateParams.builder()
                .model(model)
                .maxTokens(16000L)
                .addUserMessage("Why did this test fail?\n$testOutput")
                // Let the API retry on another model if a safety classifier declines the request
                .addBeta(SERVER_SIDE_FALLBACK_BETA)
                .putAdditionalBodyProperty("fallbacks", JsonValue.from("default"))
                .build()

        try {
            val message = client.async().beta().messages().create(params).await()

            if (message.stopReason().orElse(null) == BetaStopReason.REFUSAL) {
                logger.info("Anthropic declined to analyze test failure")
                return null
            }

            val analysis =
                message.content()
                    .mapNotNull { block -> block.text().map { it.text() }.orElse(null) }
                    .joinToString("\n")

            return if (analysis.isNotBlank()) {
                TestFailureAnalysis(analysis = analysis, promptVersion = 1)
            } else {
                logger.info("No text content found in Anthropic test failure analysis response")
                null
            }
        } catch (e: Exception) {
            logger.warn("Exception getting test failure analysis from Anthropic", e)
            return null
        }
    }

    companion object {
        const val DEFAULT_MODEL = "claude-opus-5-5"
        private const val SERVER_SIDE_FALLBACK_BETA = "server-side-fallback-2026-07-01"
    }
}
