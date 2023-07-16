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
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

class SaveGroupedResultsWithMetadataApplicationTest : ApplicationTestCase() {

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
