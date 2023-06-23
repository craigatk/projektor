package projektor.notification.github.comment

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.kotest.core.spec.style.StringSpec
import projektor.notification.github.GitHubClientConfig
import projektor.notification.github.GitHubWireMockStubber
import projektor.notification.github.WireMockTestListener
import projektor.notification.github.auth.MockJwtProvider
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import kotlin.test.assertNotNull

class GitHubCommentClientSpec : StringSpec() {
    private val wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())
    private val gitHubWireMockStubber = GitHubWireMockStubber(wireMockServer)

    override fun listeners() = listOf(WireMockTestListener(wireMockServer))

    private val jwtToken = "my-jwt"
    private val jwtProvider = MockJwtProvider(jwtToken)

    init {
        "should create comment on issue" {
            val gitHubApiUrl = "http://localhost:${wireMockServer.port()}/"
            val clientConfig = GitHubClientConfig(
                gitHubApiUrl
            )
            val gitHubCommentClient = GitHubCommentClient(clientConfig, jwtProvider)

            val orgName = "my-org"
            val repoName = "my-repo"
            val issueId = 12

            val commentText = "Here is my comment"

            gitHubWireMockStubber.stubRepositoryRequests(orgName, repoName)

            gitHubWireMockStubber.stubGetIssue(orgName, repoName, issueId)
            gitHubWireMockStubber.stubAddComment(orgName, repoName, issueId)

            val repository = gitHubCommentClient.getRepository(orgName, repoName)
            assertNotNull(repository)
            gitHubCommentClient.addComment(repository, issueId, commentText)

            val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, issueId)
            expectThat(addCommentRequestBodies).hasSize(1)

            expectThat(addCommentRequestBodies[0]).contains(commentText)
        }

        "should return null when trying to get repository that does not have app enabled" {
            val gitHubApiUrl = "http://localhost:${wireMockServer.port()}/"
            val clientConfig = GitHubClientConfig(
                gitHubApiUrl
            )
            val gitHubCommentClient = GitHubCommentClient(clientConfig, jwtProvider)

            val orgName = "my-org"
            val repoName = "app-not-installed-repo"

            gitHubWireMockStubber.stubApp()
            gitHubWireMockStubber.stubGetRepoInstallationNotFound(orgName, repoName)

            val repository = gitHubCommentClient.getRepository(orgName, repoName)
            expectThat(repository).isNull()
        }

        "when open PR exists for branch should find the PR number" {
            val gitHubApiUrl = "http://localhost:${wireMockServer.port()}/"
            val clientConfig = GitHubClientConfig(
                gitHubApiUrl
            )
            val gitHubCommentClient = GitHubCommentClient(clientConfig, jwtProvider)

            val orgName = "my-org"
            val repoName = "my-repo"

            gitHubWireMockStubber.stubRepositoryRequests(orgName, repoName)

            gitHubWireMockStubber.stubListPullRequests(orgName, repoName, listOf("the-branch", "another-branch"))

            val repository = gitHubCommentClient.getRepository(orgName, repoName)
            assertNotNull(repository)

            val pullRequestNumber = gitHubCommentClient.findOpenPullRequests(repository, "the-branch", null)
            expectThat(pullRequestNumber).isNotNull().isEqualTo(1)
        }

        "when no open PR for branch should return null for PR number" {
            val gitHubApiUrl = "http://localhost:${wireMockServer.port()}/"
            val clientConfig = GitHubClientConfig(
                gitHubApiUrl
            )
            val gitHubCommentClient = GitHubCommentClient(clientConfig, jwtProvider)

            val orgName = "my-org"
            val repoName = "my-repo"

            gitHubWireMockStubber.stubRepositoryRequests(orgName, repoName)

            gitHubWireMockStubber.stubListPullRequests(orgName, repoName, listOf("the-branch", "another-branch"))

            val repository = gitHubCommentClient.getRepository(orgName, repoName)
            assertNotNull(repository)

            val pullRequestNumber = gitHubCommentClient.findOpenPullRequests(repository, "some-branch", null)
            expectThat(pullRequestNumber).isNull()
        }
    }
}
