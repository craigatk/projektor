package projektor.incomingresults

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.ktor.http.*
import io.ktor.server.testing.*
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.notification.github.GitHubWireMockStubber
import projektor.notification.github.auth.PrivateKeyEncoder
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

class SaveGroupedResultsWithMetadataApplicationTest : ApplicationTestCase() {
    private val wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())
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
    fun `should save grouped test results with Git metadata`() {
        val gitMetadata = GitMetadata()
        gitMetadata.repoName = "craigatk/projektor"
        gitMetadata.branchName = "main"
        gitMetadata.isMainBranch = true
        val metadata = ResultsMetadata()
        metadata.git = gitMetadata
        val requestBody = GroupedResultsXmlLoader().passingGroupedResults(metadata)

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                val (_, testRun) = waitForTestRunSaveToComplete(response)

                val gitMetadatas = gitMetadataDao.fetchByTestRunId(testRun.id)
                expectThat(gitMetadatas).hasSize(1)

                val gitMetadataDB = gitMetadatas[0]
                expectThat(gitMetadataDB) {
                    get { repoName }.isEqualTo("craigatk/projektor")
                    get { orgName }.isEqualTo("craigatk")
                    get { branchName }.isEqualTo("main")
                    get { isMainBranch }.isTrue()
                }
            }
        }
    }

    @Test
    fun `should save grouped test results with Git commit SHA and pull request number`() {
        val privateKeyContents = loadTextFromFile("fake_private_key.pem")

        serverBaseUrl = "http://localhost:8080"
        gitHubApiUrl = "http://localhost:${wireMockServer.port()}"
        gitHubAppId = "12345"
        gitHubPrivateKeyEncoded = PrivateKeyEncoder.base64Encode(privateKeyContents)

        val incomingOrgName = "craigatk"
        val incomingRepoName = "projektor"
        val incomingBranchName = "main"
        val incomingCommit = "7dcdac4bb56d200cfb879da0e630210dff86e11f"

        val gitMetadata = GitMetadata()
        gitMetadata.repoName = "$incomingOrgName/$incomingRepoName"
        gitMetadata.branchName = incomingBranchName
        gitMetadata.isMainBranch = true
        gitMetadata.commitSha = incomingCommit
        val metadata = ResultsMetadata()
        metadata.git = gitMetadata
        val requestBody = GroupedResultsXmlLoader().passingGroupedResults(metadata)

        val gitHubPullRequestNumber = 2
        gitHubWireMockStubber.stubRepositoryRequests(incomingOrgName, incomingRepoName)
        gitHubWireMockStubber.stubListPullRequests(incomingOrgName, incomingRepoName, listOf("another-branch", incomingBranchName))
        gitHubWireMockStubber.stubGetIssue(incomingOrgName, incomingRepoName, gitHubPullRequestNumber)
        gitHubWireMockStubber.stubListComments(incomingOrgName, incomingRepoName, gitHubPullRequestNumber, listOf("Some other comment"))
        gitHubWireMockStubber.stubAddComment(incomingOrgName, incomingRepoName, gitHubPullRequestNumber)

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                val (_, testRun) = waitForTestRunSaveToComplete(response)

                await until { gitHubWireMockStubber.findAddCommentRequestBodies(incomingOrgName, incomingRepoName, gitHubPullRequestNumber).size == 1 }

                val gitMetadatas = gitMetadataDao.fetchByTestRunId(testRun.id)
                expectThat(gitMetadatas).hasSize(1)

                val gitMetadataDB = gitMetadatas[0]
                expectThat(gitMetadataDB) {
                    get { repoName }.isEqualTo("craigatk/projektor")
                    get { orgName }.isEqualTo("craigatk")
                    get { branchName }.isEqualTo("main")
                    get { isMainBranch }.isTrue()
                    get { commitSha }.isEqualTo(incomingCommit)
                    get { pullRequestNumber }.isEqualTo(gitHubPullRequestNumber)
                }
            }
        }
    }

    @Test
    fun `should save grouped test results with Git metadata including project name`() {
        val gitMetadata = GitMetadata()
        gitMetadata.repoName = "craigatk/projektor"
        gitMetadata.branchName = "main"
        gitMetadata.projectName = "ui-proj"
        gitMetadata.isMainBranch = true
        val metadata = ResultsMetadata()
        metadata.git = gitMetadata
        val requestBody = GroupedResultsXmlLoader().passingGroupedResults(metadata)

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                val (_, testRun) = waitForTestRunSaveToComplete(response)

                val gitMetadatas = gitMetadataDao.fetchByTestRunId(testRun.id)
                expectThat(gitMetadatas).hasSize(1)

                val gitMetadataDB = gitMetadatas[0]
                expectThat(gitMetadataDB) {
                    get { repoName }.isEqualTo("craigatk/projektor")
                    get { orgName }.isEqualTo("craigatk")
                    get { projectName }.isEqualTo("ui-proj")
                    get { branchName }.isEqualTo("main")
                    get { isMainBranch }.isTrue()
                }
            }
        }
    }

    @Test
    fun `should save grouped results with results metadata`() {
        val gitMetadata = GitMetadata()
        gitMetadata.repoName = "craigatk/projektor"
        gitMetadata.branchName = "main"
        gitMetadata.projectName = "ui-proj"
        gitMetadata.isMainBranch = true
        val metadata = ResultsMetadata()
        metadata.git = gitMetadata
        metadata.ci = true
        val requestBody = GroupedResultsXmlLoader().passingGroupedResults(metadata)

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                val (_, testRun) = waitForTestRunSaveToComplete(response)

                val resultsMetadataList = resultsMetadataDao.fetchByTestRunId(testRun.id)
                expectThat(resultsMetadataList).hasSize(1)

                val resultsMetadata = resultsMetadataList[0]
                expectThat(resultsMetadata) {
                    get { ci }.isTrue()
                }
            }
        }
    }
}
