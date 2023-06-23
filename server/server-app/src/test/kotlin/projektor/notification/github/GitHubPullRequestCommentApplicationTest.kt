package projektor.notification.github

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
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

class GitHubPullRequestCommentApplicationTest : ApplicationTestCase() {
    private val wireMockServer = WireMockServer(wireMockConfig().dynamicPort())
    private val gitHubWireMockStubber = GitHubWireMockStubber(wireMockServer)

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
    fun `when all GitHub notification properties set should publish comment to pull request`() {
        val privateKeyContents = loadTextFromFile("fake_private_key.pem")

        serverBaseUrl = "http://localhost:8080"
        gitHubApiUrl = "http://localhost:${wireMockServer.port()}"
        gitHubAppId = "12345"
        gitHubPrivateKeyEncoded = PrivateKeyEncoder.base64Encode(privateKeyContents)

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

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                val (publicId, _) = waitForTestRunSaveToComplete(response)

                await until { gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber).size == 1 }

                val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber)
                expectThat(addCommentRequestBodies).hasSize(1)
                expectThat(addCommentRequestBodies[0]).contains(publicId.id)
            }
        }
    }

    @Test
    fun `when creating a comment on the GitHub PR fails should still save test run`() {
        serverBaseUrl = "http://localhost:8080"
        gitHubApiUrl = "http://localhost:${wireMockServer.port()}"
        gitHubAppId = "12345"
        gitHubPrivateKeyEncoded = PrivateKeyEncoder.base64Encode("invalid-private-key")

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

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                waitForTestRunSaveToComplete(response)

                await until { gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber).size == 0 }
            }
        }
    }

    @Test
    fun `when report includes coverage data should include it in PR comment`() {
        val privateKeyContents = loadTextFromFile("fake_private_key.pem")

        serverBaseUrl = "http://localhost:8080"
        gitHubApiUrl = "http://localhost:${wireMockServer.port()}"
        gitHubAppId = "12345"
        gitHubPrivateKeyEncoded = PrivateKeyEncoder.base64Encode(privateKeyContents)

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

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                waitForTestRunSaveToComplete(response)

                await until { gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber).size == 1 }

                val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber)
                expectThat(addCommentRequestBodies).hasSize(1)
                expectThat(addCommentRequestBodies[0]).contains(jacocoXmlParserLineCoveragePercentage.toString())
            }
        }
    }

    @Test
    fun `when results include performance data should include it in PR comment`() {
        val privateKeyContents = loadTextFromFile("fake_private_key.pem")

        serverBaseUrl = "http://localhost:8080"
        gitHubApiUrl = "http://localhost:${wireMockServer.port()}"
        gitHubAppId = "12345"
        gitHubPrivateKeyEncoded = PrivateKeyEncoder.base64Encode(privateKeyContents)

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
        val requestBody = GroupedResultsXmlLoader().wrapPerformanceResultsInGroup("performance.json", PerformanceResultsLoader().k6GetRun(), metadata)

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                waitForTestRunSaveToComplete(response)

                await until { gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber).size == 1 }

                val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber)
                expectThat(addCommentRequestBodies).hasSize(1)
                expectThat(addCommentRequestBodies[0]).contains("p95: 22 ms, RPS: 1020")
            }
        }
    }

    @Test
    fun `should find pull request by commit SHA and add comment to it`() {
        val privateKeyContents = loadTextFromFile("fake_private_key.pem")

        serverBaseUrl = "http://localhost:8080"
        gitHubApiUrl = "http://localhost:${wireMockServer.port()}"
        gitHubAppId = "12345"
        gitHubPrivateKeyEncoded = PrivateKeyEncoder.base64Encode(privateKeyContents)

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

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                val (publicId, _) = waitForTestRunSaveToComplete(response)

                await until { gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber).size == 1 }

                val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber)
                expectThat(addCommentRequestBodies).hasSize(1)
                expectThat(addCommentRequestBodies[0]).contains(publicId.id)
            }
        }
    }

    @Test
    fun `should find pull request by pull request number and add comment to it`() {
        val privateKeyContents = loadTextFromFile("fake_private_key.pem")

        serverBaseUrl = "http://localhost:8080"
        gitHubApiUrl = "http://localhost:${wireMockServer.port()}"
        gitHubAppId = "12345"
        gitHubPrivateKeyEncoded = PrivateKeyEncoder.base64Encode(privateKeyContents)

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

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                val (publicId, _) = waitForTestRunSaveToComplete(response)

                await until { gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber).size == 1 }

                val addCommentRequestBodies = gitHubWireMockStubber.findAddCommentRequestBodies(orgName, repoName, pullRequestNumber)
                expectThat(addCommentRequestBodies).hasSize(1)
                expectThat(addCommentRequestBodies[0]).contains(publicId.id)
            }
        }
    }
}
