package projektor.incomingresults

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

@KtorExperimentalAPI
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
}
