package projektor.incomingresults

import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.server.api.results.ResultsProcessingStatus
import projektor.server.api.results.SaveResultsError
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

class SaveGroupedResultsMetricsApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should record success metric when saving grouped test results succeeds`() =
        testSuspend {
            val requestBody = GroupedResultsXmlLoader().passingGroupedResults()

            val response = postGroupedResultsJSON(requestBody)

            waitForTestRunSaveToComplete(response)

            expectThat(meterRegistry.counter("results_process_start").count()).isEqualTo(1.toDouble())
            expectThat(meterRegistry.counter("results_process_success").count()).isEqualTo(1.toDouble())
            expectThat(meterRegistry.counter("results_process_failure").count()).isEqualTo(0.toDouble())
            expectThat(meterRegistry.counter("results_parse_failure").count()).isEqualTo(0.toDouble())
        }

    @Test
    fun `when test results fail to parse should record parse error metric`() =
        testSuspend {
            val malformedResults = GroupedResultsXmlLoader().passingGroupedResults().replace("testsuite", "")

            val response = postGroupedResultsJSON(malformedResults, HttpStatusCode.BadRequest)

            val resultsResponse = objectMapper.readValue(response.bodyAsText(), SaveResultsError::class.java)

            val publicId = resultsResponse.id
            assertNotNull(publicId)

            await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.ERROR.name }

            expectThat(meterRegistry.counter("results_process_start").count()).isEqualTo(1.toDouble())
            expectThat(meterRegistry.counter("results_parse_failure").count()).isEqualTo(1.toDouble())
            expectThat(meterRegistry.counter("results_process_failure").count()).isEqualTo(0.toDouble())
            expectThat(meterRegistry.counter("results_process_success").count()).isEqualTo(0.toDouble())
        }

    @Test
    fun `when test results fail to parse because they are cut off should record parse error metric`() =
        testSuspend {
            val malformedResults = GroupedResultsXmlLoader().wrapResultsXmlInGroup(ResultsXmlLoader().cutOffResultsGradle())

            val response = postGroupedResultsJSON(malformedResults, HttpStatusCode.BadRequest)

            val resultsResponse = objectMapper.readValue(response.bodyAsText(), SaveResultsError::class.java)

            val publicId = resultsResponse.id
            assertNotNull(publicId)

            await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.ERROR.name }

            expectThat(meterRegistry.counter("results_process_start").count()).isEqualTo(1.toDouble())
            expectThat(meterRegistry.counter("results_parse_failure").count()).isEqualTo(1.toDouble())
            expectThat(meterRegistry.counter("results_process_failure").count()).isEqualTo(0.toDouble())
            expectThat(meterRegistry.counter("results_process_success").count()).isEqualTo(0.toDouble())
        }
}
