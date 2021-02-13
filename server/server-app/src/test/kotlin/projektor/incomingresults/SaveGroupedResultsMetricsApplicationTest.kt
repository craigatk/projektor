package projektor.incomingresults

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.server.api.results.ResultsProcessingStatus
import projektor.server.api.results.SaveResultsError
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

@KtorExperimentalAPI
class SaveGroupedResultsMetricsApplicationTest : ApplicationTestCase() {

    @Test
    fun `should record success metric when saving grouped test results succeeds`() {
        val requestBody = GroupedResultsXmlLoader().passingGroupedResults()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                waitForTestRunSaveToComplete(response)

                expectThat(meterRegistry.counter("results_process_success").count()).isEqualTo(1.toDouble())
                expectThat(meterRegistry.counter("results_process_failure").count()).isEqualTo(0.toDouble())
            }
        }
    }

    @Test
    fun `when test results fail to parse should record parse error metric`() {
        val malformedResults = GroupedResultsXmlLoader().passingGroupedResults().replace("testsuite", "")

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(malformedResults)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                val resultsResponse = objectMapper.readValue(response.content, SaveResultsError::class.java)

                val publicId = resultsResponse.id
                assertNotNull(publicId)

                await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.ERROR.name }

                expectThat(meterRegistry.counter("results_parse_failure").count()).isEqualTo(1.toDouble())
                expectThat(meterRegistry.counter("results_process_success").count()).isEqualTo(0.toDouble())
            }
        }
    }
}
