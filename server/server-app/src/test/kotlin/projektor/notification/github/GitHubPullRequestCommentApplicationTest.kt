package projektor.notification.github

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.ApplicationTestCaseConfig
import projektor.notification.github.auth.PrivateKeyEncoder
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.CoverageFile
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.server.example.coverage.JacocoXmlLoader.Companion.jacocoXmlParserLineCoveragePercentage
import projektor.server.example.performance.PerformanceResultsLoader
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize

@Disabled("For whatever reason this test intermittently fails with a Hikari error")
class GitHubPullRequestCommentApplicationTest : ApplicationTestCase() {
    private val wireMockServer = WireMockServer(wireMockConfig().dynamicPort())
    private val gitHubWireMockStubber = GitHubWireMockStubber(WireMock(wireMockServer))

    @BeforeEach
    fun startWireMock() {
        wireMockServer.start()
        wireMockServer.resetAll()
    }

    @AfterEach
    fun stopWireMock() {
        wireMockServer.stop()
    }

    @Test
    fun `when all GitHub notification properties set should publish comment to pull request`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                serverBaseUrl = "http://localhost:8080",
                gitHubApiUrl = "http://localhost:${wireMockServer.port()}",
                gitHubAppId = "12345",
                gitHubPrivateKeyEncoded = PrivateKeyEncoder.base64Encode(loadTextFromFile("fake_private_key.pem")),
            ),
        ) {
            val orgName = "craigatk"
            val repoName = "projektor"
            val branchName = "main"

            val pullRequestNumber = 2
            gitHubWireMockStubber.stubRepositoryRequests(orgName, repoName)
            gitHubWireMockStubber.stubListPullRequests(orgName, repoName, listOf("another-branch", branchName))
            gitHubWireMockStubber.stubGetIssue(orgName, repoName, pullRequestNumber)
            gitHubWireMockStubber.stubListComments(orgName, repoName, pullRequestNumber, listOf("Some other comment"))
            gitHubWireMockStubber.stubAddComment(orgName, repoName, pullRequestNumber)

            val gitMetadata = GitMetadata()
            gitMetadata.repoName = "$orgName/$repoName"
            gitMetadata.branchName = branchName
            gitMetadata.isMainBranch = true
            val metadata = ResultsMetadata()
            metadata.git = gitMetadata
            val requestBody = GroupedResultsXmlLoader().passingGroupedResults(metadata)

            val response = client.postGroupedResultsJSON(requestBody)
            val (publicId, _) = waitForTestRunSaveToComplete(response)

            await until { gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber).size == 1 }

            val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber)
            expectThat(addCommentRequestBodies).hasSize(1)
            expectThat(addCommentRequestBodies[0]).contains(publicId.id)
        }

    @Test
    fun `when creating a comment on the GitHub PR fails should still save test run`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                serverBaseUrl = "http://localhost:8080",
                gitHubApiUrl = "http://localhost:${wireMockServer.port()}",
                gitHubAppId = "12345",
                gitHubPrivateKeyEncoded = PrivateKeyEncoder.base64Encode("invalid-key"),
            ),
        ) {
            val orgName = "craigatk"
            val repoName = "projektor"
            val branchName = "main"

            val pullRequestNumber = 2
            gitHubWireMockStubber.stubRepositoryRequests(orgName, repoName)
            gitHubWireMockStubber.stubListPullRequests(orgName, repoName, listOf("another-branch", branchName))
            gitHubWireMockStubber.stubGetIssue(orgName, repoName, pullRequestNumber)
            gitHubWireMockStubber.stubListComments(orgName, repoName, pullRequestNumber, listOf("Some other comment"))
            gitHubWireMockStubber.stubAddComment(orgName, repoName, pullRequestNumber)

            val gitMetadata = GitMetadata()
            gitMetadata.repoName = "$orgName/$repoName"
            gitMetadata.branchName = branchName
            gitMetadata.isMainBranch = true
            val metadata = ResultsMetadata()
            metadata.git = gitMetadata
            val requestBody = GroupedResultsXmlLoader().passingGroupedResults(metadata)

            val response = client.postGroupedResultsJSON(requestBody)

            waitForTestRunSaveToComplete(response)

            await until { gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber).size == 0 }
        }

    @Test
    fun `when report includes coverage data should include it in PR comment`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                serverBaseUrl = "http://localhost:8080",
                gitHubApiUrl = "http://localhost:${wireMockServer.port()}",
                gitHubAppId = "12345",
                gitHubPrivateKeyEncoded = PrivateKeyEncoder.base64Encode(loadTextFromFile("fake_private_key.pem")),
            ),
        ) {
            val orgName = "craigatk"
            val repoName = "projektor"
            val branchName = "main"

            val pullRequestNumber = 2
            gitHubWireMockStubber.stubRepositoryRequests(orgName, repoName)
            gitHubWireMockStubber.stubListPullRequests(orgName, repoName, listOf("another-branch", branchName))
            gitHubWireMockStubber.stubGetIssue(orgName, repoName, pullRequestNumber)
            gitHubWireMockStubber.stubListComments(orgName, repoName, pullRequestNumber, listOf("Some other comment"))
            gitHubWireMockStubber.stubAddComment(orgName, repoName, pullRequestNumber)

            val coverageFile = CoverageFile()
            coverageFile.reportContents = JacocoXmlLoader().jacocoXmlParser()

            val gitMetadata = GitMetadata()
            gitMetadata.repoName = "$orgName/$repoName"
            gitMetadata.branchName = branchName
            gitMetadata.isMainBranch = true
            val metadata = ResultsMetadata()
            metadata.git = gitMetadata
            val requestBody = GroupedResultsXmlLoader().passingResultsWithCoverage(listOf(coverageFile), metadata)

            val response = client.postGroupedResultsJSON(requestBody)
            waitForTestRunSaveToComplete(response)

            await until { gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber).size == 1 }

            val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber)
            expectThat(addCommentRequestBodies).hasSize(1)
            expectThat(addCommentRequestBodies[0]).contains(jacocoXmlParserLineCoveragePercentage.toString())
        }

    @Test
    fun `when results include performance data should include it in PR comment`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                serverBaseUrl = "http://localhost:8080",
                gitHubApiUrl = "http://localhost:${wireMockServer.port()}",
                gitHubAppId = "12345",
                gitHubPrivateKeyEncoded = PrivateKeyEncoder.base64Encode(loadTextFromFile("fake_private_key.pem")),
            ),
        ) {
            val orgName = "craigatk"
            val repoName = "projektor"
            val branchName = "main"

            val pullRequestNumber = 2
            gitHubWireMockStubber.stubRepositoryRequests(orgName, repoName)
            gitHubWireMockStubber.stubListPullRequests(orgName, repoName, listOf("another-branch", branchName))
            gitHubWireMockStubber.stubGetIssue(orgName, repoName, pullRequestNumber)
            gitHubWireMockStubber.stubListComments(orgName, repoName, pullRequestNumber, listOf("Some other comment"))
            gitHubWireMockStubber.stubAddComment(orgName, repoName, pullRequestNumber)

            val gitMetadata = GitMetadata()
            gitMetadata.repoName = "$orgName/$repoName"
            gitMetadata.branchName = branchName
            gitMetadata.isMainBranch = true
            val metadata = ResultsMetadata()
            metadata.git = gitMetadata
            val requestBody =
                GroupedResultsXmlLoader().wrapPerformanceResultsInGroup(
                    "performance.json",
                    PerformanceResultsLoader().k6GetRun(),
                    metadata,
                )

            val response = client.postGroupedResultsJSON(requestBody)
            waitForTestRunSaveToComplete(response)

            await until { gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber).size == 1 }

            val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber)
            expectThat(addCommentRequestBodies).hasSize(1)
            expectThat(addCommentRequestBodies[0]).contains("p95: 22 ms, RPS: 1020")
        }

    @Test
    fun `should find pull request by commit SHA and add comment to it`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                serverBaseUrl = "http://localhost:8080",
                gitHubApiUrl = "http://localhost:${wireMockServer.port()}",
                gitHubAppId = "12345",
                gitHubPrivateKeyEncoded = PrivateKeyEncoder.base64Encode(loadTextFromFile("fake_private_key.pem")),
            ),
        ) {
            val orgName = "craigatk"
            val repoName = "projektor"
            val branchName = "main"
            val commitSha = "123456789"

            val pullRequestNumber = 2
            gitHubWireMockStubber.stubRepositoryRequests(orgName, repoName)
            gitHubWireMockStubber.stubListPullRequests(orgName, repoName, listOf("branch-1", "branch-2"), listOf("sha-1", commitSha))
            gitHubWireMockStubber.stubGetIssue(orgName, repoName, pullRequestNumber)
            gitHubWireMockStubber.stubListComments(orgName, repoName, pullRequestNumber, listOf("Some other comment"))
            gitHubWireMockStubber.stubAddComment(orgName, repoName, pullRequestNumber)

            val gitMetadata = GitMetadata()
            gitMetadata.repoName = "$orgName/$repoName"
            gitMetadata.branchName = branchName
            gitMetadata.isMainBranch = true
            gitMetadata.commitSha = commitSha
            val metadata = ResultsMetadata()
            metadata.git = gitMetadata
            val requestBody = GroupedResultsXmlLoader().passingGroupedResults(metadata)

            val response = client.postGroupedResultsJSON(requestBody)
            val (publicId, _) = waitForTestRunSaveToComplete(response)

            await until { gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber).size == 1 }

            val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber)
            expectThat(addCommentRequestBodies).hasSize(1)
            expectThat(addCommentRequestBodies[0]).contains(publicId.id)
        }

    @Test
    fun `should find pull request by pull request number and add comment to it`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                serverBaseUrl = "http://localhost:8080",
                gitHubApiUrl = "http://localhost:${wireMockServer.port()}",
                gitHubAppId = "12345",
                gitHubPrivateKeyEncoded = PrivateKeyEncoder.base64Encode(loadTextFromFile("fake_private_key.pem")),
            ),
        ) {
            val orgName = "craigatk"
            val repoName = "projektor"
            val pullRequestNumber = 2
            gitHubWireMockStubber.stubRepositoryRequests(orgName, repoName)
            gitHubWireMockStubber.stubGetIssue(orgName, repoName, pullRequestNumber)
            gitHubWireMockStubber.stubListComments(orgName, repoName, pullRequestNumber, listOf("Some other comment"))
            gitHubWireMockStubber.stubAddComment(orgName, repoName, pullRequestNumber)

            val gitMetadata = GitMetadata()
            gitMetadata.repoName = "$orgName/$repoName"
            gitMetadata.isMainBranch = false
            gitMetadata.pullRequestNumber = pullRequestNumber
            val metadata = ResultsMetadata()
            metadata.git = gitMetadata
            val requestBody = GroupedResultsXmlLoader().passingGroupedResults(metadata)

            val response = client.postGroupedResultsJSON(requestBody)
            val (publicId, _) = waitForTestRunSaveToComplete(response)

            await until { gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber).size == 1 }

            val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber)
            expectThat(addCommentRequestBodies).hasSize(1)
            expectThat(addCommentRequestBodies[0]).contains(publicId.id)
        }
}
