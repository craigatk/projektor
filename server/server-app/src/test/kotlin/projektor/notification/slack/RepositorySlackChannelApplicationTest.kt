package projektor.notification.slack

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.ApplicationTestCaseConfig
import projektor.server.api.repository.RepositorySlackChannel
import projektor.server.api.repository.SaveRepositorySlackChannelError
import projektor.util.randomOrgAndRepo
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

class RepositorySlackChannelApplicationTest : ApplicationTestCase() {
    private val wireMockServer = WireMockServer(wireMockConfig().dynamicPort())
    private val slackWireMockStubber = SlackWireMockStubber(WireMock(wireMockServer))

    @BeforeEach
    fun startWireMock() {
        wireMockServer.start()
        wireMockServer.resetAll()
    }

    @AfterEach
    fun stopWireMock() {
        wireMockServer.stop()
    }

    private fun slackEnabledConfig() =
        ApplicationTestCaseConfig(
            serverBaseUrl = "http://localhost:8080",
            slackApiUrl = "http://localhost:${wireMockServer.port()}",
            slackBotToken = "xoxb-token",
        )

    private suspend fun HttpClient.putChannel(
        uri: String,
        channelName: String,
    ): HttpResponse =
        put(uri) {
            contentType(ContentType.Application.Json)
            setBody(objectMapper.writeValueAsString(RepositorySlackChannel(channelName)))
        }

    private suspend fun HttpResponse.channelName(): String =
        objectMapper.readValue(bodyAsText(), RepositorySlackChannel::class.java).channelName

    @Test
    fun `should save, fetch, and delete Slack channel for repo`() =
        projektorTestApplication(slackEnabledConfig()) {
            val repoName = randomOrgAndRepo()
            val uri = "/repo/$repoName/notification/slack"
            slackWireMockStubber.stubPostMessageSuccess()

            expectThat(client.get(uri).status).isEqualTo(HttpStatusCode.NoContent)

            val saveResponse = client.putChannel(uri, "#build-alerts")
            expectThat(saveResponse.status).isEqualTo(HttpStatusCode.OK)
            expectThat(saveResponse.channelName()).isEqualTo("build-alerts")

            val confirmationRequestBodies = slackWireMockStubber.findPostMessageRequestBodies()
            expectThat(confirmationRequestBodies).hasSize(1)
            expectThat(confirmationRequestBodies[0]).contains("\"channel\":\"build-alerts\"").contains(repoName)

            val fetchResponse = client.get(uri)
            expectThat(fetchResponse.status).isEqualTo(HttpStatusCode.OK)
            expectThat(fetchResponse.channelName()).isEqualTo("build-alerts")

            expectThat(client.putChannel(uri, "other-alerts").status).isEqualTo(HttpStatusCode.OK)
            expectThat(client.get(uri).channelName()).isEqualTo("other-alerts")

            expectThat(client.delete(uri).status).isEqualTo(HttpStatusCode.OK)
            expectThat(client.get(uri).status).isEqualTo(HttpStatusCode.NoContent)
        }

    @Test
    fun `should save Slack channels for repo and project separately`() =
        projektorTestApplication(slackEnabledConfig()) {
            val repoName = randomOrgAndRepo()
            val repoUri = "/repo/$repoName/notification/slack"
            val projectUri = "/repo/$repoName/project/ui/notification/slack"
            slackWireMockStubber.stubPostMessageSuccess()

            expectThat(client.putChannel(projectUri, "ui-alerts").status).isEqualTo(HttpStatusCode.OK)

            expectThat(client.get(projectUri).channelName()).isEqualTo("ui-alerts")
            expectThat(client.get(repoUri).status).isEqualTo(HttpStatusCode.NoContent)
            expectThat(client.get("/repo/$repoName/project/server/notification/slack").status).isEqualTo(HttpStatusCode.NoContent)

            expectThat(client.putChannel(repoUri, "repo-alerts").status).isEqualTo(HttpStatusCode.OK)

            expectThat(client.get(projectUri).channelName()).isEqualTo("ui-alerts")
            expectThat(client.get(repoUri).channelName()).isEqualTo("repo-alerts")

            expectThat(client.delete(projectUri).status).isEqualTo(HttpStatusCode.OK)
            expectThat(client.get(projectUri).status).isEqualTo(HttpStatusCode.NoContent)
            expectThat(client.get(repoUri).channelName()).isEqualTo("repo-alerts")
        }

    @Test
    fun `should reject invalid Slack channel name`() =
        projektorTestApplication(slackEnabledConfig()) {
            val uri = "/repo/${randomOrgAndRepo()}/notification/slack"
            slackWireMockStubber.stubPostMessageSuccess()

            val saveResponse = client.putChannel(uri, "not a channel!")
            expectThat(saveResponse.status).isEqualTo(HttpStatusCode.BadRequest)

            expectThat(slackWireMockStubber.findPostMessageRequestBodies()).isEmpty()
            expectThat(client.get(uri).status).isEqualTo(HttpStatusCode.NoContent)
        }

    @Test
    fun `should not save Slack channel when Slack cannot post to it`() =
        projektorTestApplication(slackEnabledConfig()) {
            val uri = "/repo/${randomOrgAndRepo()}/notification/slack"
            slackWireMockStubber.stubPostMessageError("not_in_channel")

            val saveResponse = client.putChannel(uri, "build-alerts")
            expectThat(saveResponse.status).isEqualTo(HttpStatusCode.BadRequest)

            val error = objectMapper.readValue(saveResponse.bodyAsText(), SaveRepositorySlackChannelError::class.java)
            expectThat(error.errorMessage).contains("not_in_channel")

            expectThat(client.get(uri).status).isEqualTo(HttpStatusCode.NoContent)
        }

    @Test
    fun `should return not found when Slack is not enabled`() =
        projektorTestApplication {
            val uri = "/repo/${randomOrgAndRepo()}/notification/slack"
            val projectUri = "/repo/${randomOrgAndRepo()}/project/ui/notification/slack"

            expectThat(client.get(uri).status).isEqualTo(HttpStatusCode.NotFound)
            expectThat(client.putChannel(uri, "build-alerts").status).isEqualTo(HttpStatusCode.NotFound)
            expectThat(client.delete(uri).status).isEqualTo(HttpStatusCode.NotFound)
            expectThat(client.get(projectUri).status).isEqualTo(HttpStatusCode.NotFound)
        }

    @Test
    fun `should not enable Slack when only API URL is set`() =
        projektorTestApplication(ApplicationTestCaseConfig(slackApiUrl = "http://localhost:${wireMockServer.port()}")) {
            expectThat(client.get("/repo/${randomOrgAndRepo()}/notification/slack").status).isEqualTo(HttpStatusCode.NotFound)
        }
}
