package projektor.notification.github

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import io.kotest.core.spec.style.StringSpec
import projektor.notification.github.auth.MockJwtProvider
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize

class GitHubCommentClientSpec : StringSpec() {
    private val wireMockServer = WireMockServer(wireMockConfig().dynamicPort())
    private val gitHubWireMockStubber = GitHubWireMockStubber(wireMockServer)

    override fun listeners() = listOf(WireMockTestListener(wireMockServer))

    init {
        "should create comment on issue" {
            val gitHubApiUrl = "http://localhost:${wireMockServer.port()}/"

            val orgName = "my-org"
            val repoName = "my-repo"
            val issueId = 12

            val jwtToken = "my-jwt"

            val jwtProvider = MockJwtProvider(jwtToken)

            val clientConfig = GitHubClientConfig(
                gitHubApiUrl
            )

            val gitHubCommentClient = GitHubCommentClient(clientConfig, jwtProvider)

            val commentText = "Here is my comment"

            val installationId = 1
            val accessToken = "v1.1f699f1069f60xxx"
            val repositoryId = 2
            gitHubWireMockStubber.stubApp()
            gitHubWireMockStubber.stubGetRepoInstallation(orgName, repoName, installationId)
            gitHubWireMockStubber.stubCreateInstallationAccessToken(installationId, accessToken, repositoryId)

            gitHubWireMockStubber.stubGetRepository(orgName, repoName, repositoryId)
            gitHubWireMockStubber.stubCreateInstallationAccessToken(repositoryId, accessToken, repositoryId)

            gitHubWireMockStubber.stubGetIssue(orgName, repoName, issueId)
            gitHubWireMockStubber.stubAddComment(orgName, repoName, issueId)

            val repository = gitHubCommentClient.getRepository(orgName, repoName)
            gitHubCommentClient.addComment(repository, issueId, commentText)

            val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, issueId)
            expectThat(addCommentRequestBodies).hasSize(1)

            expectThat(addCommentRequestBodies[0]).contains(commentText)
        }
    }
}
