package projektor.incomingresults

import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.LocalDateTime
import java.time.ZoneOffset

class SaveGroupedResultsCreatedTimestampTest : ApplicationTestCase() {
    @Test
    fun `should set created timestamp when passed in`() =
        projektorTestApplication {
            val gitMetadata = GitMetadata()
            gitMetadata.repoName = "craigatk/projektor"
            gitMetadata.branchName = "main"
            gitMetadata.isMainBranch = true

            val createdTimestamp = LocalDateTime.of(2024, 12, 15, 12, 0).toInstant(ZoneOffset.UTC)
            val metadata = ResultsMetadata()
            metadata.git = gitMetadata
            metadata.createdTimestamp = createdTimestamp

            val requestBody = GroupedResultsXmlLoader().passingGroupedResults(metadata)

            val response1 = client.postGroupedResultsJSON(requestBody)
            val (_, testRun) = waitForTestRunSaveToComplete(response1)

            expectThat(testRun.createdTimestamp.toInstant(ZoneOffset.UTC)).isEqualTo(createdTimestamp)
        }
}
