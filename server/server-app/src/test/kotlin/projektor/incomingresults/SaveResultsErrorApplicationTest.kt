package projektor.incomingresults

import io.ktor.client.statement.*
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.awaitility.kotlin.untilNotNull
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.server.api.results.ResultsProcessingStatus
import projektor.server.api.results.SaveResultsResponse
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

class SaveResultsErrorApplicationTest : ApplicationTestCase() {
    @Test
    fun `should still process results after multiple failures`() =
        projektorTestApplication {
            val malformedResults = resultsXmlLoader.passing().replace("testsuite", "")
            val successfulResults = resultsXmlLoader.passing()

            (1..10).forEach { _ ->
                val response = client.postResultsPlainText(malformedResults)

                val resultsResponse = objectMapper.readValue(response.bodyAsText(), SaveResultsResponse::class.java)

                val publicId = resultsResponse.id
                assertNotNull(publicId)

                await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.ERROR.name }

                val resultsProcessingFailure = await untilNotNull { resultsProcessingFailureDao.fetchOneByPublicId(publicId) }
                expectThat(resultsProcessingFailure.body).isEqualTo(malformedResults)
            }

            (1..10).forEach { _ ->
                val response = client.postResultsPlainText(successfulResults)

                val resultsResponse = objectMapper.readValue(response.bodyAsText(), SaveResultsResponse::class.java)

                val publicId = resultsResponse.id
                assertNotNull(publicId)

                await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.SUCCESS.name }
            }
        }
}
