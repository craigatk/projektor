package projektor.notification.github.comment

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Test
import projektor.notification.github.GitHubClientConfig
import projektor.notification.github.GitHubWireMockStubber
import projektor.notification.github.auth.MockJwtProvider
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.assertNotNull

@WireMockTest
class GitHubCommentServiceTest {
    private val jwtToken = "my-jwt"
    private val jwtProvider = MockJwtProvider(jwtToken)

    @Test
    fun `should add new comment when one does not exist`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val gitHubWireMockStubber = GitHubWireMockStubber(wmRuntimeInfo.wireMock)

        val gitHubApiUrl = "http://localhost:${wmRuntimeInfo.httpPort}/"
        val clientConfig =
            GitHubClientConfig(
                gitHubApiUrl,
            )
        val commentClient = GitHubCommentClient(clientConfig, jwtProvider)
        val commentService = GitHubCommentService(commentClient)

        val orgName = "my-org"
        val repoName = "my-repo"
        val branchName = "my-branch"
        val pullRequestNumber = 2
        val report =
            ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git =
                    ReportCommentGitData(
                        orgName = orgName,
                        repoName = repoName,
                        branchName = branchName,
                    ),
                publicId = "V1BMYK93MTNR",
                createdDate =
                    LocalDateTime.of(
                        LocalDate.of(2020, 12, 16),
                        LocalTime.of(14, 30),
                    ),
                passed = true,
                failedTestCount = 0,
                totalTestCount = 25,
                coverage = null,
                performance = null,
                project = null,
            )

        gitHubWireMockStubber.stubRepositoryRequests(orgName, repoName)
        gitHubWireMockStubber.stubListPullRequests(orgName, repoName, listOf("another-branch", branchName))
        gitHubWireMockStubber.stubGetIssue(orgName, repoName, pullRequestNumber)
        gitHubWireMockStubber.stubListComments(orgName, repoName, pullRequestNumber, listOf("Some other comment"))
        gitHubWireMockStubber.stubAddComment(orgName, repoName, pullRequestNumber)

        val pullRequest = commentService.upsertReportComment(report, "12345")
        assertNotNull(pullRequest)
        expectThat(pullRequest.orgName).isEqualTo(orgName)
        expectThat(pullRequest.repoName).isEqualTo(repoName)
        expectThat(pullRequest.number).isEqualTo(pullRequestNumber)

        val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber)
        expectThat(addCommentRequestBodies).hasSize(1)
        expectThat(addCommentRequestBodies[0]).contains("V1BMYK93MTNR")

        val updateCommentRequestBodies = gitHubWireMockStubber.findUpdateCommentRequestBodies(orgName, repoName, 1)
        expectThat(updateCommentRequestBodies).hasSize(0)
    }

    @Test
    fun `should update comment when one exists`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val gitHubWireMockStubber = GitHubWireMockStubber(wmRuntimeInfo.wireMock)

        val gitHubApiUrl = "http://localhost:${wmRuntimeInfo.httpPort}/"
        val clientConfig =
            GitHubClientConfig(
                gitHubApiUrl,
            )
        val commentClient = GitHubCommentClient(clientConfig, jwtProvider)
        val commentService = GitHubCommentService(commentClient)

        val orgName = "my-org"
        val repoName = "my-repo"
        val branchName = "my-branch"
        val pullRequestNumber = 2
        val report =
            ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git =
                    ReportCommentGitData(
                        orgName = orgName,
                        repoName = repoName,
                        branchName = branchName,
                    ),
                publicId = "V1BMYK93MTNR",
                createdDate =
                    LocalDateTime.of(
                        LocalDate.of(2020, 12, 16),
                        LocalTime.of(14, 30),
                    ),
                passed = true,
                failedTestCount = 0,
                totalTestCount = 25,
                coverage = null,
                performance = null,
                project = null,
            )

        gitHubWireMockStubber.stubRepositoryRequests(orgName, repoName)
        gitHubWireMockStubber.stubListPullRequests(orgName, repoName, listOf("another-branch", branchName))
        gitHubWireMockStubber.stubGetIssue(orgName, repoName, pullRequestNumber)
        val commentId = 3
        gitHubWireMockStubber.stubListComments(
            orgName,
            repoName,
            pullRequestNumber,
            listOf("A comment", "Another comment", GitHubCommentCreator.HEADER_TEXT),
        )
        gitHubWireMockStubber.stubGetComment(orgName, repoName, pullRequestNumber, commentId, GitHubCommentCreator.HEADER_TEXT)
        gitHubWireMockStubber.stubUpdateComment(orgName, repoName, pullRequestNumber, commentId)

        val pullRequest = commentService.upsertReportComment(report, "12345")
        assertNotNull(pullRequest)
        expectThat(pullRequest.orgName).isEqualTo(orgName)
        expectThat(pullRequest.repoName).isEqualTo(repoName)
        expectThat(pullRequest.number).isEqualTo(pullRequestNumber)

        val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber)
        expectThat(addCommentRequestBodies).hasSize(0)

        val updateCommentRequestBodies = gitHubWireMockStubber.findUpdateCommentRequestBodies(orgName, repoName, commentId)
        expectThat(updateCommentRequestBodies).hasSize(1)
        expectThat(updateCommentRequestBodies[0]).contains("V1BMYK93MTNR")
    }

    @Test
    fun `should return null when no pull request with branch name`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val gitHubWireMockStubber = GitHubWireMockStubber(wmRuntimeInfo.wireMock)

        val gitHubApiUrl = "http://localhost:${wmRuntimeInfo.httpPort}/"
        val clientConfig =
            GitHubClientConfig(
                gitHubApiUrl,
            )
        val commentClient = GitHubCommentClient(clientConfig, jwtProvider)
        val commentService = GitHubCommentService(commentClient)

        val orgName = "my-org"
        val repoName = "my-repo"
        val branchName = "my-branch"
        val pullRequestNumber = 2
        val report =
            ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git =
                    ReportCommentGitData(
                        orgName = orgName,
                        repoName = repoName,
                        branchName = branchName,
                    ),
                publicId = "V1BMYK93MTNR",
                createdDate =
                    LocalDateTime.of(
                        LocalDate.of(2020, 12, 16),
                        LocalTime.of(14, 30),
                    ),
                passed = true,
                failedTestCount = 0,
                totalTestCount = 25,
                coverage = null,
                performance = null,
                project = null,
            )

        gitHubWireMockStubber.stubRepositoryRequests(orgName, repoName)
        gitHubWireMockStubber.stubListPullRequests(orgName, repoName, listOf("another-branch", "yet-another-branch"))
        gitHubWireMockStubber.stubGetIssue(orgName, repoName, pullRequestNumber)
        gitHubWireMockStubber.stubListComments(orgName, repoName, pullRequestNumber, listOf("Some other comment"))
        gitHubWireMockStubber.stubAddComment(orgName, repoName, pullRequestNumber)

        val pullRequest = commentService.upsertReportComment(report, "12345")
        expectThat(pullRequest).isNull()

        val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber)
        expectThat(addCommentRequestBodies).hasSize(0)

        val updateCommentRequestBodies = gitHubWireMockStubber.findUpdateCommentRequestBodies(orgName, repoName, 1)
        expectThat(updateCommentRequestBodies).hasSize(0)
    }

    @Test
    fun `should find pull request by commit SHA`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val gitHubWireMockStubber = GitHubWireMockStubber(wmRuntimeInfo.wireMock)

        val gitHubApiUrl = "http://localhost:${wmRuntimeInfo.httpPort}/"
        val clientConfig =
            GitHubClientConfig(
                gitHubApiUrl,
            )
        val commentClient = GitHubCommentClient(clientConfig, jwtProvider)
        val commentService = GitHubCommentService(commentClient)

        val orgName = "my-org"
        val repoName = "my-repo"
        val branchName = "my-branch"
        val pullRequestNumber = 2
        val sha = "123456789"
        val report =
            ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git =
                    ReportCommentGitData(
                        orgName = orgName,
                        repoName = repoName,
                        branchName = branchName,
                        commitSha = sha,
                    ),
                publicId = "V1BMYK93MTNR",
                createdDate =
                    LocalDateTime.of(
                        LocalDate.of(2020, 12, 16),
                        LocalTime.of(14, 30),
                    ),
                passed = true,
                failedTestCount = 0,
                totalTestCount = 25,
                coverage = null,
                performance = null,
                project = null,
            )

        gitHubWireMockStubber.stubRepositoryRequests(orgName, repoName)
        gitHubWireMockStubber.stubListPullRequests(orgName, repoName, listOf("branch-1", "branch-2"), listOf("sha1", sha))
        gitHubWireMockStubber.stubGetIssue(orgName, repoName, pullRequestNumber)
        gitHubWireMockStubber.stubListComments(orgName, repoName, pullRequestNumber, listOf("Some other comment"))
        gitHubWireMockStubber.stubAddComment(orgName, repoName, pullRequestNumber)

        val pullRequest = commentService.upsertReportComment(report, "12345")
        assertNotNull(pullRequest)
        expectThat(pullRequest.orgName).isEqualTo(orgName)
        expectThat(pullRequest.repoName).isEqualTo(repoName)
        expectThat(pullRequest.number).isEqualTo(pullRequestNumber)

        val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber)
        expectThat(addCommentRequestBodies).hasSize(1)
        expectThat(addCommentRequestBodies[0]).contains("V1BMYK93MTNR")

        val updateCommentRequestBodies = gitHubWireMockStubber.findUpdateCommentRequestBodies(orgName, repoName, 1)
        expectThat(updateCommentRequestBodies).hasSize(0)
    }

    @Test
    fun `should find pull request by commit SHA when no branch included`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val gitHubWireMockStubber = GitHubWireMockStubber(wmRuntimeInfo.wireMock)

        val gitHubApiUrl = "http://localhost:${wmRuntimeInfo.httpPort}/"
        val clientConfig =
            GitHubClientConfig(
                gitHubApiUrl,
            )
        val commentClient = GitHubCommentClient(clientConfig, jwtProvider)
        val commentService = GitHubCommentService(commentClient)

        val orgName = "my-org"
        val repoName = "my-repo"
        val branchName = null
        val pullRequestNumber = 2
        val sha = "123456789"
        val report =
            ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git =
                    ReportCommentGitData(
                        orgName = orgName,
                        repoName = repoName,
                        branchName = branchName,
                        commitSha = sha,
                    ),
                publicId = "V1BMYK93MTNR",
                createdDate =
                    LocalDateTime.of(
                        LocalDate.of(2020, 12, 16),
                        LocalTime.of(14, 30),
                    ),
                passed = true,
                failedTestCount = 0,
                totalTestCount = 25,
                coverage = null,
                performance = null,
                project = null,
            )

        gitHubWireMockStubber.stubRepositoryRequests(orgName, repoName)
        gitHubWireMockStubber.stubListPullRequests(orgName, repoName, listOf("branch-1", "branch-2"), listOf("sha1", sha))
        gitHubWireMockStubber.stubGetIssue(orgName, repoName, pullRequestNumber)
        gitHubWireMockStubber.stubListComments(orgName, repoName, pullRequestNumber, listOf("Some other comment"))
        gitHubWireMockStubber.stubAddComment(orgName, repoName, pullRequestNumber)

        val pullRequest = commentService.upsertReportComment(report, "12345")
        assertNotNull(pullRequest)
        expectThat(pullRequest.orgName).isEqualTo(orgName)
        expectThat(pullRequest.repoName).isEqualTo(repoName)
        expectThat(pullRequest.number).isEqualTo(pullRequestNumber)

        val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber)
        expectThat(addCommentRequestBodies).hasSize(1)
        expectThat(addCommentRequestBodies[0]).contains("V1BMYK93MTNR")

        val updateCommentRequestBodies = gitHubWireMockStubber.findUpdateCommentRequestBodies(orgName, repoName, 1)
        expectThat(updateCommentRequestBodies).hasSize(0)
    }

    @Test
    fun `when pull request number passed in should use that`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val gitHubWireMockStubber = GitHubWireMockStubber(wmRuntimeInfo.wireMock)

        val gitHubApiUrl = "http://localhost:${wmRuntimeInfo.httpPort}/"
        val clientConfig =
            GitHubClientConfig(
                gitHubApiUrl,
            )
        val commentClient = GitHubCommentClient(clientConfig, jwtProvider)
        val commentService = GitHubCommentService(commentClient)

        val orgName = "my-org"
        val repoName = "my-repo"
        val branchName = "my-branch"
        val pullRequestNumber = 2
        val sha = "123456789"
        val report =
            ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git =
                    ReportCommentGitData(
                        orgName = orgName,
                        repoName = repoName,
                        branchName = branchName,
                        commitSha = sha,
                        pullRequestNumber = pullRequestNumber,
                    ),
                publicId = "V1BMYK93MTNR",
                createdDate =
                    LocalDateTime.of(
                        LocalDate.of(2020, 12, 16),
                        LocalTime.of(14, 30),
                    ),
                passed = true,
                failedTestCount = 0,
                totalTestCount = 25,
                coverage = null,
                performance = null,
                project = null,
            )

        gitHubWireMockStubber.stubRepositoryRequests(orgName, repoName)
        gitHubWireMockStubber.stubGetIssue(orgName, repoName, pullRequestNumber)
        gitHubWireMockStubber.stubListComments(orgName, repoName, pullRequestNumber, listOf("Some other comment"))
        gitHubWireMockStubber.stubAddComment(orgName, repoName, pullRequestNumber)

        val pullRequest = commentService.upsertReportComment(report, "12345")
        assertNotNull(pullRequest)
        expectThat(pullRequest.orgName).isEqualTo(orgName)
        expectThat(pullRequest.repoName).isEqualTo(repoName)
        expectThat(pullRequest.number).isEqualTo(pullRequestNumber)

        val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber)
        expectThat(addCommentRequestBodies).hasSize(1)
        expectThat(addCommentRequestBodies[0]).contains("V1BMYK93MTNR")

        val updateCommentRequestBodies = gitHubWireMockStubber.findUpdateCommentRequestBodies(orgName, repoName, 1)
        expectThat(updateCommentRequestBodies).hasSize(0)
    }
}
