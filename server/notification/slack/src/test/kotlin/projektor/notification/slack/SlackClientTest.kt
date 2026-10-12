package projektor.notification.slack

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Test
import projektor.notification.slack.message.SlackMessage
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@WireMockTest
class SlackClientTest {
    private val botToken = "xoxb-my-token"

    @Test
    fun `should post message to channel with bot token`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val slackWireMockStubber = SlackWireMockStubber(wmRuntimeInfo.wireMock)
        slackWireMockStubber.stubPostMessageSuccess()

        val slackClient = SlackClient(SlackClientConfig("http://localhost:${wmRuntimeInfo.httpPort}/", botToken))

        slackClient.postMessage(SlackMessage(channel = "my-channel", text = "Here is my message"))

        val requestBodies = slackWireMockStubber.findPostMessageRequestBodies()
        expectThat(requestBodies).hasSize(1)
        expectThat(requestBodies[0]).contains("\"channel\":\"my-channel\"").contains("Here is my message")

        expectThat(slackWireMockStubber.findPostMessageAuthorizationHeaders()).isEqualTo(listOf("Bearer $botToken"))
    }

    @Test
    fun `should throw when Slack responds with ok false`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val slackWireMockStubber = SlackWireMockStubber(wmRuntimeInfo.wireMock)
        slackWireMockStubber.stubPostMessageError("not_in_channel")

        val slackClient = SlackClient(SlackClientConfig("http://localhost:${wmRuntimeInfo.httpPort}", botToken))

        expectThrows<SlackApiException> {
            slackClient.postMessage(SlackMessage(channel = "my-channel", text = "Here is my message"))
        }.get { message }.isNotNull().contains("not_in_channel")
    }

    @Test
    fun `should throw when Slack responds with error status`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val slackWireMockStubber = SlackWireMockStubber(wmRuntimeInfo.wireMock)
        slackWireMockStubber.stubPostMessageStatus(500)

        val slackClient = SlackClient(SlackClientConfig("http://localhost:${wmRuntimeInfo.httpPort}", botToken))

        expectThrows<SlackApiException> {
            slackClient.postMessage(SlackMessage(channel = "my-channel", text = "Here is my message"))
        }.get { message }.isNotNull().contains("500")
    }
}
