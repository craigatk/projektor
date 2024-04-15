package projektor.notification.github.comment

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Test
import projektor.notification.github.GitHubClientConfig
import projektor.notification.github.GitHubWireMockStubber
import projektor.notification.github.auth.MockJwtProvider
import strikt.api.expectThat
import strikt.assertions.*
import kotlin.test.assertNotNull

@WireMockTest
class GitHubCommentClientTest {
    private val jwtToken = "my-jwt"
    private val jwtProvider = MockJwtProvider(jwtToken)

    @Test
    fun `should create comment on issue`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val gitHubWireMockStubber = GitHubWireMockStubber(wmRuntimeInfo.wireMock)

        val gitHubApiUrl = "http://localhost:${wmRuntimeInfo.httpPort}/"
        val clientConfig =
            GitHubClientConfig(
                gitHubApiUrl,
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

    @Test
    fun `should return null when trying to get repository that does not have app enabled`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val gitHubWireMockStubber = GitHubWireMockStubber(wmRuntimeInfo.wireMock)

        val gitHubApiUrl = "http://localhost:${wmRuntimeInfo.httpPort}/"
        val clientConfig =
            GitHubClientConfig(
                gitHubApiUrl,
            )
        val gitHubCommentClient = GitHubCommentClient(clientConfig, jwtProvider)

        val orgName = "my-org"
        val repoName = "app-not-installed-repo"

        gitHubWireMockStubber.stubApp()
        gitHubWireMockStubber.stubGetRepoInstallationNotFound(orgName, repoName)

        val repository = gitHubCommentClient.getRepository(orgName, repoName)
        expectThat(repository).isNull()
    }

    @Test
    fun `when open PR exists for branch should find the PR number`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val gitHubWireMockStubber = GitHubWireMockStubber(wmRuntimeInfo.wireMock)

        val gitHubApiUrl = "http://localhost:${wmRuntimeInfo.httpPort}/"
        val clientConfig =
            GitHubClientConfig(
                gitHubApiUrl,
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

    @Test
    fun `when no open PR for branch should return null for PR number`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val gitHubWireMockStubber = GitHubWireMockStubber(wmRuntimeInfo.wireMock)

        val gitHubApiUrl = "http://localhost:${wmRuntimeInfo.httpPort}/"
        val clientConfig =
            GitHubClientConfig(
                gitHubApiUrl,
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
