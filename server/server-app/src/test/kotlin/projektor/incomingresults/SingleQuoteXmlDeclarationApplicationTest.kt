package projektor.incomingresults

import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class SingleQuoteXmlDeclarationApplicationTest : ApplicationTestCase() {
    @Test
    fun `should save results with single-quoted XML declaration`() =
        projektorTestApplication {
            val gitMetadata = GitMetadata()
            gitMetadata.repoName = "craigatk/projektor"
            gitMetadata.branchName = "main"
            gitMetadata.isMainBranch = true
            val metadata = ResultsMetadata()
            metadata.git = gitMetadata

            val resultXml = ResultsXmlLoader().singleQuoteXmlDeclaration()
            val requestBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultXml, metadata)

            val response = client.postGroupedResultsJSON(requestBody)
            val (_, testRun) = waitForTestRunSaveToComplete(response)

            val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)

            expectThat(testSuites).hasSize(1)

            expectThat(testSuites[0]) {
                get { className }.isEqualTo("SingleQuoteSpec")
            }
        }
}
