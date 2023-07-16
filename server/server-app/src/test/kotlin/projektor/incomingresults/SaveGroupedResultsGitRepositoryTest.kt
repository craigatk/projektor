package projektor.incomingresults

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class SaveGroupedResultsGitRepositoryTest : ApplicationTestCase() {

    @Test
    fun `should set Git repository table with Git org and repo`() {
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
                waitForTestRunSaveToComplete(response)

                val gitRepositoryDB = gitRepositoryDao.fetchOneByRepoName("craigatk/projektor")
                expectThat(gitRepositoryDB).isNotNull().and {
                    get { orgName }.isEqualTo("craigatk")
                }
            }
        }

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                waitForTestRunSaveToComplete(response)

                val gitRepositoryDB = gitRepositoryDao.fetchOneByRepoName("craigatk/projektor")
                expectThat(gitRepositoryDB).isNotNull().and {
                    get { orgName }.isEqualTo("craigatk")
                }
            }
        }

        val gitMetadata2 = GitMetadata()
        gitMetadata2.repoName = "craigatk/projektor-action"
        gitMetadata2.branchName = "main"
        gitMetadata2.isMainBranch = true
        val metadata2 = ResultsMetadata()
        metadata2.git = gitMetadata2
        val requestBody2 = GroupedResultsXmlLoader().passingGroupedResults(metadata2)

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody2)
            }.apply {
                waitForTestRunSaveToComplete(response)

                val gitRepositoryDB = gitRepositoryDao.fetchOneByRepoName("craigatk/projektor-action")
                expectThat(gitRepositoryDB).isNotNull().and {
                    get { orgName }.isEqualTo("craigatk")
                }
            }
        }
    }
}
