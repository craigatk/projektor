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
import strikt.assertions.isNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.assertNotNull

class GitHubCommentServiceSpec : StringSpec() {
    private val wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())
    private val gitHubWireMockStubber = GitHubWireMockStubber(wireMockServer)

    override fun listeners() = listOf(WireMockTestListener(wireMockServer))

    private val jwtToken = "my-jwt"
    private val jwtProvider = MockJwtProvider(jwtToken)

    init {
        "should add new comment when one does not exist" {
            val gitHubApiUrl = "http://localhost:${wireMockServer.port()}/"
            val clientConfig = GitHubClientConfig(
                gitHubApiUrl
            )
            val commentClient = GitHubCommentClient(clientConfig, jwtProvider)
            val commentService = GitHubCommentService(commentClient)

            val orgName = "my-org"
            val repoName = "my-repo"
            val branchName = "my-branch"
            val pullRequestNumber = 2
            val report = ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git = ReportCommentGitData(
                    orgName = orgName,
                    repoName = repoName,
                    branchName = branchName
                ),
                publicId = "V1BMYK93MTNR",
                createdDate = LocalDateTime.of(
                    LocalDate.of(2020, 12, 16),
                    LocalTime.of(14, 30)
                ),
                passed = true,
                failedTestCount = 0,
                totalTestCount = 25,
                coverage = null,
                performance = null,
                project = null
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

        "should update comment when one exists" {
            val gitHubApiUrl = "http://localhost:${wireMockServer.port()}/"
            val clientConfig = GitHubClientConfig(
                gitHubApiUrl
            )
            val commentClient = GitHubCommentClient(clientConfig, jwtProvider)
            val commentService = GitHubCommentService(commentClient)

            val orgName = "my-org"
            val repoName = "my-repo"
            val branchName = "my-branch"
            val pullRequestNumber = 2
            val report = ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git = ReportCommentGitData(
                    orgName = orgName,
                    repoName = repoName,
                    branchName = branchName
                ),
                publicId = "V1BMYK93MTNR",
                createdDate = LocalDateTime.of(
                    LocalDate.of(2020, 12, 16),
                    LocalTime.of(14, 30)
                ),
                passed = true,
                failedTestCount = 0,
                totalTestCount = 25,
                coverage = null,
                performance = null,
                project = null
            )

            gitHubWireMockStubber.stubRepositoryRequests(orgName, repoName)
            gitHubWireMockStubber.stubListPullRequests(orgName, repoName, listOf("another-branch", branchName))
            gitHubWireMockStubber.stubGetIssue(orgName, repoName, pullRequestNumber)
            val commentId = 3
            gitHubWireMockStubber.stubListComments(orgName, repoName, pullRequestNumber, listOf("A comment", "Another comment", GitHubCommentCreator.headerText))
            gitHubWireMockStubber.stubGetComment(orgName, repoName, pullRequestNumber, commentId, GitHubCommentCreator.headerText)
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

        "should return null when no pull request with branch name" {
            val gitHubApiUrl = "http://localhost:${wireMockServer.port()}/"
            val clientConfig = GitHubClientConfig(
                gitHubApiUrl
            )
            val commentClient = GitHubCommentClient(clientConfig, jwtProvider)
            val commentService = GitHubCommentService(commentClient)

            val orgName = "my-org"
            val repoName = "my-repo"
            val branchName = "my-branch"
            val pullRequestNumber = 2
            val report = ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git = ReportCommentGitData(
                    orgName = orgName,
                    repoName = repoName,
                    branchName = branchName
                ),
                publicId = "V1BMYK93MTNR",
                createdDate = LocalDateTime.of(
                    LocalDate.of(2020, 12, 16),
                    LocalTime.of(14, 30)
                ),
                passed = true,
                failedTestCount = 0,
                totalTestCount = 25,
                coverage = null,
                performance = null,
                project = null
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

        "should find pull request by commit SHA" {
            val gitHubApiUrl = "http://localhost:${wireMockServer.port()}/"
            val clientConfig = GitHubClientConfig(
                gitHubApiUrl
            )
            val commentClient = GitHubCommentClient(clientConfig, jwtProvider)
            val commentService = GitHubCommentService(commentClient)

            val orgName = "my-org"
            val repoName = "my-repo"
            val branchName = "my-branch"
            val pullRequestNumber = 2
            val sha = "123456789"
            val report = ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git = ReportCommentGitData(
                    orgName = orgName,
                    repoName = repoName,
                    branchName = branchName,
                    commitSha = sha
                ),
                publicId = "V1BMYK93MTNR",
                createdDate = LocalDateTime.of(
                    LocalDate.of(2020, 12, 16),
                    LocalTime.of(14, 30)
                ),
                passed = true,
                failedTestCount = 0,
                totalTestCount = 25,
                coverage = null,
                performance = null,
                project = null
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

        "should find pull request by commit SHA when no branch included" {
            val gitHubApiUrl = "http://localhost:${wireMockServer.port()}/"
            val clientConfig = GitHubClientConfig(
                gitHubApiUrl
            )
            val commentClient = GitHubCommentClient(clientConfig, jwtProvider)
            val commentService = GitHubCommentService(commentClient)

            val orgName = "my-org"
            val repoName = "my-repo"
            val branchName = null
            val pullRequestNumber = 2
            val sha = "123456789"
            val report = ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git = ReportCommentGitData(
                    orgName = orgName,
                    repoName = repoName,
                    branchName = branchName,
                    commitSha = sha
                ),
                publicId = "V1BMYK93MTNR",
                createdDate = LocalDateTime.of(
                    LocalDate.of(2020, 12, 16),
                    LocalTime.of(14, 30)
                ),
                passed = true,
                failedTestCount = 0,
                totalTestCount = 25,
                coverage = null,
                performance = null,
                project = null
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

        "when pull request number passed in should use that" {
            val gitHubApiUrl = "http://localhost:${wireMockServer.port()}/"
            val clientConfig = GitHubClientConfig(
                gitHubApiUrl
            )
            val commentClient = GitHubCommentClient(clientConfig, jwtProvider)
            val commentService = GitHubCommentService(commentClient)

            val orgName = "my-org"
            val repoName = "my-repo"
            val branchName = "my-branch"
            val pullRequestNumber = 2
            val sha = "123456789"
            val report = ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git = ReportCommentGitData(
                    orgName = orgName,
                    repoName = repoName,
                    branchName = branchName,
                    commitSha = sha,
                    pullRequestNumber = pullRequestNumber
                ),
                publicId = "V1BMYK93MTNR",
                createdDate = LocalDateTime.of(
                    LocalDate.of(2020, 12, 16),
                    LocalTime.of(14, 30)
                ),
                passed = true,
                failedTestCount = 0,
                totalTestCount = 25,
                coverage = null,
                performance = null,
                project = null
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
}
