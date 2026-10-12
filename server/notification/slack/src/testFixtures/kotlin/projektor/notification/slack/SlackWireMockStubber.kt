package projektor.notification.slack

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

class SlackWireMockStubber(private val wireMockServer: WireMock) {
    fun stubPostMessageSuccess() {
        wireMockServer.register(
            post(urlEqualTo(POST_MESSAGE_PATH))
                .willReturn(
                    aResponse().withHeader("Content-Type", "application/json").withBody("""{"ok": true, "ts": "1503435956.000247"}"""),
                ),
        )
    }

    fun stubPostMessageError(error: String) {
        wireMockServer.register(
            post(urlEqualTo(POST_MESSAGE_PATH))
                .willReturn(jsonResponse("""{"ok": false, "error": "$error"}""")),
        )
    }

    fun stubPostMessageStatus(status: Int) {
        wireMockServer.register(post(urlEqualTo(POST_MESSAGE_PATH)).willReturn(aResponse().withStatus(status)))
    }

    fun findPostMessageRequestBodies(): List<String> =
        wireMockServer.find(postRequestedFor(urlEqualTo(POST_MESSAGE_PATH))).map { it.bodyAsString }

    fun findPostMessageAuthorizationHeaders(): List<String> =
        wireMockServer.find(postRequestedFor(urlEqualTo(POST_MESSAGE_PATH))).map { it.getHeader("Authorization") }

    private fun jsonResponse(body: String) = aResponse().withHeader("Content-Type", "application/json").withBody(body)

    companion object {
        const val POST_MESSAGE_PATH = "/chat.postMessage"
    }
}
