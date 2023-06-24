package projektor.incomingresults

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.isEqualTo

class SaveGroupedResultsTelemetryApplicationTest : ApplicationTestCase() {

    @Test
    fun `should record telemetry when saving grouped test results`() {
        val requestBody = GroupedResultsXmlLoader().passingGroupedResults()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                waitForTestRunSaveToComplete(response)

                val finishedSpans = exporter.finishedSpanItems

                expectThat(finishedSpans)
                    .any {
                        get { name }.isEqualTo("projektor.parseGroupedResults")
                    }
            }
        }
    }
}
