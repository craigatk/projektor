package projektor.ai.openai

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@WireMockTest
class OpenAIAnalysisClientTest {
    @Test
    fun `should get test failure analysis from mocked API`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val client =
            OpenAIAnalysisClient(
                "api-key",
                wmRuntimeInfo.httpBaseUrl,
            )

        val openAIWireMockStubber = OpenAIWireMockStubber(wmRuntimeInfo.wireMock)

        openAIWireMockStubber.stubForSingleChatCompletion("Failure analysis")

        val failureAnalysis = runBlocking { client.analyzeTestFailure("my-test-failure") }
        expectThat(failureAnalysis).isNotNull().get { analysis }.isEqualTo("Failure analysis")

        expectThat(wmRuntimeInfo.wireMock.findAllUnmatchedRequests()).isEmpty()
    }
}
