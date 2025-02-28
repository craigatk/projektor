package projektor.ai.openai

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.openai.models.ChatCompletion
import com.openai.models.ChatCompletion.Choice
import com.openai.models.ChatCompletionMessage
import com.openai.models.ChatModel
import java.util.*

class OpenAIWireMockStubber(private val wireMockServer: WireMock) {
    fun stubForSingleChatCompletion(content: String) {
        val objectMapper = ObjectMapper()
        val chatCompletionResponse =
            ChatCompletion.builder()
                .choices(
                    listOf(
                        Choice.builder()
                            .message(
                                ChatCompletionMessage.builder().content(content).refusal("").build(),
                            )
                            .finishReason(Choice.FinishReason.LENGTH)
                            .index(0)
                            .logprobs(Optional.empty())
                            .build(),
                    ),
                )
                .id("id")
                .created(System.currentTimeMillis() / 1000)
                .model(ChatModel.GPT_4O_MINI.toString())
                .build()

        wireMockServer.register(
            post(
                urlEqualTo("/chat/completions"),
            ).willReturn(aResponse().withStatus(200).withBody(objectMapper.writeValueAsString(chatCompletionResponse))),
        )
    }
}
