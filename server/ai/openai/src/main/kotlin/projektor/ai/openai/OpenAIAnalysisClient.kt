package projektor.ai.openai

import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.credential.BearerTokenCredential
import com.openai.models.ChatModel
import com.openai.models.chat.completions.ChatCompletionCreateParams
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import projektor.ai.analysis.AITestFailureAnalyzer
import projektor.ai.analysis.TestFailureAnalysis
import kotlin.jvm.optionals.getOrNull

class OpenAIAnalysisClient(
    apiKey: String,
    baseUrl: String? = null,
    bearerToken: String? = null,
) : AITestFailureAnalyzer {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    private val client: OpenAIClient =
        if (baseUrl == null) {
            OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build()
        } else {
            OpenAIOkHttpClient.builder().baseUrl(baseUrl).credential(
                BearerTokenCredential.Companion.create(bearerToken ?: ""),
            ).build()
        }

    override suspend fun analyzeTestFailure(testOutput: String): TestFailureAnalysis? {
        val params =
            ChatCompletionCreateParams.builder()
                .addUserMessage("Why did this test fail?\n$testOutput")
                .model(ChatModel.GPT_4O_MINI)
                .build()

        try {
            val chatCompletion = client.async().chat().completions().create(params).await()

            val choices = chatCompletion.choices()

            return if (choices.isNotEmpty()) {
                val firstChoiceContent = choices[0].message().content()

                firstChoiceContent.map { content -> TestFailureAnalysis(analysis = content, promptVersion = 1) }.getOrNull()
            } else {
                logger.info("No choices found in OpenAI test failure analysis response")
                null
            }
        } catch (e: Exception) {
            logger.warn("Exception getting test failure analysis from OpenAI", e)
            return null
        }
    }
}
