package projektor.incomingresults

import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class SaveGroupedResultsGitRepositoryTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should set Git repository table with Git org and repo`() =
        testSuspend {
            val gitMetadata = GitMetadata()
            gitMetadata.repoName = "craigatk/projektor"
            gitMetadata.branchName = "main"
            gitMetadata.isMainBranch = true
            val metadata = ResultsMetadata()
            metadata.git = gitMetadata
            val requestBody = GroupedResultsXmlLoader().passingGroupedResults(metadata)

            val response1 = postGroupedResultsJSON(requestBody)

            waitForTestRunSaveToComplete(response1)

            val gitRepositoryDB = gitRepositoryDao.fetchOneByRepoName("craigatk/projektor")
            expectThat(gitRepositoryDB).isNotNull().and {
                get { orgName }.isEqualTo("craigatk")
            }

            val gitMetadata2 = GitMetadata()
            gitMetadata2.repoName = "craigatk/projektor-action"
            gitMetadata2.branchName = "main"
            gitMetadata2.isMainBranch = true
            val metadata2 = ResultsMetadata()
            metadata2.git = gitMetadata2
            val requestBody2 = GroupedResultsXmlLoader().passingGroupedResults(metadata2)

            val response2 = postGroupedResultsJSON(requestBody2)
            waitForTestRunSaveToComplete(response2)

            val gitRepositoryDB2 = gitRepositoryDao.fetchOneByRepoName("craigatk/projektor-action")
            expectThat(gitRepositoryDB2).isNotNull().and {
                get { orgName }.isEqualTo("craigatk")
            }
        }
}
