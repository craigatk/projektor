package projektor.incomingresults

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.ApplicationTestCaseConfig
import projektor.notification.github.GitHubWireMockStubber
import projektor.notification.github.auth.PrivateKeyEncoder
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import projektor.server.api.metadata.TestRunGitMetadata
import projektor.server.api.results.SaveResultsResponse
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

class SaveGroupedResultsWithMetadataPullRequestCommentApplicationTest : ApplicationTestCase() {
    @BeforeEach
    fun reset() {
        wireMockServer.resetAll()
    }

    @Test
    fun `should save grouped test results with Git commit SHA and pull request number and make pull request comment`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                serverBaseUrl = "http://localhost:8080",
                gitHubApiUrl = "http://localhost:${wireMockServer.port()}",
                gitHubAppId = "12345",
                gitHubPrivateKeyEncoded = PrivateKeyEncoder.base64Encode(loadTextFromFile("fake_private_key.pem")),
            ),
        ) {
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

            val response = client.postGroupedResultsJSON(requestBody)
            val (_, testRun) = waitForTestRunSaveToComplete(response)

            await until {
                gitHubWireMockStubber.findAddCommentRequestBodies(
                    incomingOrgName,
                    incomingRepoName,
                    gitHubPullRequestNumber,
                ).size == 1
            }

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

    @Test
    fun `should save pull request number from comment with object-store persistence`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                serverBaseUrl = "http://localhost:8080",
                gitHubApiUrl = "http://localhost:${wireMockServer.port()}",
                gitHubAppId = "12345",
                gitHubPrivateKeyEncoded = PrivateKeyEncoder.base64Encode(loadTextFromFile("fake_private_key.pem")),
                objectStorePersistence = true,
            ),
        ) {
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

            val response = client.postGroupedResultsJSON(requestBody)
            val publicId = objectMapper.readValue(response.bodyAsText(), SaveResultsResponse::class.java).id

            await until {
                gitHubWireMockStubber.findAddCommentRequestBodies(incomingOrgName, incomingRepoName, gitHubPullRequestNumber).size == 1
            }

            // The pull request number found when commenting is saved after the comment is made
            await until {
                runBlocking {
                    val gitMetadataResponse = client.get("/run/$publicId/metadata/git")
                    gitMetadataResponse.status == HttpStatusCode.OK &&
                        objectMapper.readValue(
                            gitMetadataResponse.bodyAsText(),
                            TestRunGitMetadata::class.java,
                        ).pullRequestNumber == gitHubPullRequestNumber
                }
            }

            val savedGitMetadata =
                objectMapper.readValue(
                    client.get("/run/$publicId/metadata/git").bodyAsText(),
                    TestRunGitMetadata::class.java,
                )
            expectThat(savedGitMetadata) {
                get { repoName }.isEqualTo("craigatk/projektor")
                get { orgName }.isEqualTo("craigatk")
                get { commitSha }.isEqualTo(incomingCommit)
            }
        }

    companion object {
        private val wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())
        private val gitHubWireMockStubber = GitHubWireMockStubber(WireMock(wireMockServer))

        @BeforeAll
        @JvmStatic
        fun start() {
            wireMockServer.start()
        }

        @AfterAll
        @JvmStatic
        fun stop() {
            wireMockServer.stop()
        }
    }
}
