package projektor.incomingresults

import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.isEqualTo

class SaveGroupedResultsTelemetryApplicationTest : ApplicationTestCase() {
    @Test
    fun `should record telemetry when saving grouped test results`() =
        projektorTestApplication {
            val requestBody = GroupedResultsXmlLoader().passingGroupedResults()

            val response = client.postGroupedResultsJSON(requestBody)
            waitForTestRunSaveToComplete(response)

            val finishedSpans = exporter.finishedSpanItems

            expectThat(finishedSpans)
                .any {
                    get { name }.isEqualTo("projektor.parseGroupedResults")
                }
        }
}
