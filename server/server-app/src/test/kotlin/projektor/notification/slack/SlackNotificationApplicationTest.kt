package projektor.notification.slack

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.ApplicationTestCaseConfig
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import projektor.server.api.repository.RepositorySlackChannel
import projektor.util.randomOrgAndRepo
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class SlackNotificationApplicationTest : ApplicationTestCase() {
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

    private fun mainlineMetadata(repoName: String): ResultsMetadata {
        val gitMetadata = GitMetadata()
        gitMetadata.repoName = repoName
        gitMetadata.branchName = "main"
        gitMetadata.isMainBranch = true
        val metadata = ResultsMetadata()
        metadata.git = gitMetadata
        metadata.ci = true

        return metadata
    }

    @Test
    fun `when mainline run fails should send notification to mapped Slack channel`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                serverBaseUrl = "http://localhost:8080",
                slackApiUrl = "http://localhost:${wireMockServer.port()}",
                slackBotToken = "xoxb-token",
            ),
        ) {
            val repoName = randomOrgAndRepo()
            slackWireMockStubber.stubPostMessageSuccess()

            val saveChannelResponse =
                client.put("/repo/$repoName/notification/slack") {
                    contentType(ContentType.Application.Json)
                    setBody(objectMapper.writeValueAsString(RepositorySlackChannel("build-alerts")))
                }
            expectThat(saveChannelResponse.status).isEqualTo(HttpStatusCode.OK)

            val requestBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXmlLoader.failing(), mainlineMetadata(repoName))

            val response = client.postGroupedResultsJSON(requestBody)
            val (publicId, _) = waitForTestRunSaveToComplete(response)

            await until { slackWireMockStubber.findPostMessageRequestBodies().any { it.contains(publicId.id) } }

            val notificationRequestBodies = slackWireMockStubber.findPostMessageRequestBodies().filter { it.contains(publicId.id) }
            expectThat(notificationRequestBodies).hasSize(1)
            expectThat(notificationRequestBodies[0])
                .contains("\"channel\":\"build-alerts\"")
                .contains("Mainline went red")
                .contains("http://localhost:8080/tests/${publicId.id}/")

            await until { meterRegistry.counter("slack_notification_success").count() == 1.0 }
        }

    @Test
    fun `when sending Slack notification fails should still save test run`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                serverBaseUrl = "http://localhost:8080",
                slackApiUrl = "http://localhost:${wireMockServer.port()}",
                slackBotToken = "xoxb-token",
            ),
        ) {
            val repoName = randomOrgAndRepo()
            slackWireMockStubber.stubPostMessageSuccess()

            client.put("/repo/$repoName/notification/slack") {
                contentType(ContentType.Application.Json)
                setBody(objectMapper.writeValueAsString(RepositorySlackChannel("build-alerts")))
            }

            slackWireMockStubber.stubPostMessageError("channel_not_found")

            val requestBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXmlLoader.failing(), mainlineMetadata(repoName))

            val response = client.postGroupedResultsJSON(requestBody)
            waitForTestRunSaveToComplete(response)

            await until { meterRegistry.counter("slack_notification_failure").count() == 1.0 }
        }
}
