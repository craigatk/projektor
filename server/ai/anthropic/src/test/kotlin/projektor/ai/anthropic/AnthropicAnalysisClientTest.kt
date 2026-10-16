package projektor.ai.anthropic

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

@WireMockTest
class AnthropicAnalysisClientTest {
    @Test
    fun `should get test failure analysis from mocked API`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val client = AnthropicAnalysisClient("api-key", wmRuntimeInfo.httpBaseUrl)

        stubMessageResponse(wmRuntimeInfo, stopReason = "end_turn", text = "Failure analysis")

        val failureAnalysis = runBlocking { client.analyzeTestFailure("my-test-failure") }
        expectThat(failureAnalysis).isNotNull().get { analysis }.isEqualTo("Failure analysis")

        wmRuntimeInfo.wireMock.verifyThat(
            postRequestedFor(urlPathEqualTo("/v1/messages"))
                .withHeader("x-api-key", containing("api-key"))
                .withRequestBody(containing("my-test-failure")),
        )
        expectThat(wmRuntimeInfo.wireMock.findAllUnmatchedRequests()).isEmpty()
    }

    @Test
    fun `should return null when request is refused`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val client = AnthropicAnalysisClient("api-key", wmRuntimeInfo.httpBaseUrl)

        stubMessageResponse(wmRuntimeInfo, stopReason = "refusal", text = null)

        val failureAnalysis = runBlocking { client.analyzeTestFailure("my-test-failure") }
        expectThat(failureAnalysis).isNull()
    }

    private fun stubMessageResponse(
        wmRuntimeInfo: WireMockRuntimeInfo,
        stopReason: String,
        text: String?,
    ) {
        val content = text?.let { """[{"type":"text","text":"$it"}]""" } ?: "[]"

        val body =
            """
            {
              "id": "msg_123",
              "type": "message",
              "role": "assistant",
              "model": "${AnthropicAnalysisClient.DEFAULT_MODEL}",
              "content": $content,
              "stop_reason": "$stopReason",
              "stop_sequence": null,
              "usage": {"input_tokens": 10, "output_tokens": 5}
            }
            """.trimIndent()

        wmRuntimeInfo.wireMock.register(
            post(urlPathEqualTo("/v1/messages"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(body)),
        )
    }
}
