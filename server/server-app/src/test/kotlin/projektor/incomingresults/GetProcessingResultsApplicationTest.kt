package projektor.incomingresults

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.awaitility.kotlin.untilNotNull
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.server.api.results.ResultsProcessing
import projektor.server.api.results.ResultsProcessingStatus
import projektor.server.api.results.SaveResultsResponse
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isLessThanOrEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.time.LocalDateTime
import kotlin.test.assertNotNull

class GetProcessingResultsApplicationTest : ApplicationTestCase() {
    @Test
    fun `should get processing results after parsing saving test results`() =
        projektorTestApplication {
            val beforeSubmission = LocalDateTime.now()

            val requestBody = resultsXmlLoader.passing()

            val postResponse = client.postResultsPlainText(requestBody)

            val resultsResponse = objectMapper.readValue(postResponse.bodyAsText(), SaveResultsResponse::class.java)

            val publicId = resultsResponse.id
            assertNotNull(publicId)

            await untilNotNull { testRunDao.fetchOneByPublicId(publicId) }

            await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.SUCCESS.name }

            val getResponse = client.get("/results/$publicId/status")
            expectThat(getResponse.status).isEqualTo(HttpStatusCode.OK)

            val processingResponse = objectMapper.readValue(getResponse.bodyAsText(), ResultsProcessing::class.java)

            val afterProcessing = LocalDateTime.now()

            expectThat(processingResponse)
                .isNotNull()
                .and {
                    get { status }.isEqualTo(ResultsProcessingStatus.SUCCESS)
                    get { errorMessage }.isNull()
                    get { createdTimestamp }.isNotNull()
                        .isGreaterThanOrEqualTo(beforeSubmission)
                        .isLessThanOrEqualTo(afterProcessing)
                }
        }

    @Test
    fun `when trying to fetch processing results status for ID that does not exist should return 404 response`() =
        projektorTestApplication {
            val publicId = "doesNotExist"

            val response = client.get("/results/$publicId/status")
            expectThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        }
}
