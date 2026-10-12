package projektor.notification.slack

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import projektor.notification.slack.message.SlackMessage
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class SlackClient(
    private val clientConfig: SlackClientConfig,
    private val httpClient: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
) {
    private val objectMapper: ObjectMapper =
        jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun postMessage(message: SlackMessage) {
        val request =
            HttpRequest.newBuilder(URI.create("${clientConfig.apiUrl.trimEnd('/')}/chat.postMessage"))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer ${clientConfig.botToken}")
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(message)))
                .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() !in 200..299) {
            throw SlackApiException("Slack API returned status ${response.statusCode()}")
        }

        // Slack reports most failures (unknown channel, bot not in channel, etc.) as a 200 with ok=false
        val slackResponse = objectMapper.readValue(response.body(), SlackResponse::class.java)

        if (!slackResponse.ok) {
            throw SlackApiException("Slack API returned error: ${slackResponse.error ?: "unknown"}")
        }
    }

    private data class SlackResponse(val ok: Boolean = false, val error: String? = null)
}
