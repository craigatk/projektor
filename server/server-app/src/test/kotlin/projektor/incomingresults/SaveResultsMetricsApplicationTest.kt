package projektor.incomingresults

import io.ktor.client.statement.*
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.server.api.results.ResultsProcessingStatus
import projektor.server.api.results.SaveResultsResponse
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

class SaveResultsMetricsApplicationTest : ApplicationTestCase() {
    @Test
    fun `should record success metric when saving grouped test results succeeds`() =
        projektorTestApplication {
            val requestBody = resultsXmlLoader.passing()

            val response = client.postResultsPlainText(requestBody)

            waitForTestRunSaveToComplete(response)

            expectThat(meterRegistry.counter("results_process_success").count()).isEqualTo(1.toDouble())
            expectThat(meterRegistry.counter("results_process_failure").count()).isEqualTo(0.toDouble())
            expectThat(meterRegistry.counter("results_parse_failure").count()).isEqualTo(0.toDouble())
        }

    @Test
    fun `when test results fail to parse because they are cut off should record parse error metric`() =
        projektorTestApplication {
            val malformedResults = resultsXmlLoader.cutOffResultsGradle()

            val response = client.postResultsPlainText(malformedResults)

            val resultsResponse = objectMapper.readValue(response.bodyAsText(), SaveResultsResponse::class.java)

            val publicId = resultsResponse.id
            assertNotNull(publicId)

            await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.ERROR.name }

            expectThat(meterRegistry.counter("results_parse_failure").count()).isEqualTo(1.toDouble())
            expectThat(meterRegistry.counter("results_process_failure").count()).isEqualTo(0.toDouble())
            expectThat(meterRegistry.counter("results_process_success").count()).isEqualTo(0.toDouble())
        }
}
