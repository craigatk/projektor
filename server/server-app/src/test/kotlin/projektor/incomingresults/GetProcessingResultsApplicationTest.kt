package projektor.incomingresults

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
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
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.time.LocalDateTime
import kotlin.test.assertNotNull

class GetProcessingResultsApplicationTest : ApplicationTestCase() {
    @Test
    fun `should get processing results after parsing saving test results`() {
        val requestBody = resultsXmlLoader.passing()

        withTestApplication(::createTestApplication) {
            var publicId: String

            handleRequest(HttpMethod.Post, "/results") {
                addHeader(HttpHeaders.ContentType, "text/plain")
                setBody(requestBody)
            }.apply {
                val resultsResponse = objectMapper.readValue(response.content, SaveResultsResponse::class.java)

                publicId = resultsResponse.id
                assertNotNull(publicId)
            }

            await untilNotNull { testRunDao.fetchOneByPublicId(publicId) }

            await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.SUCCESS.name }

            handleRequest(HttpMethod.Get, "/results/$publicId/status")
                .apply {
                    expectThat(response) {
                        get { status() }.isEqualTo(HttpStatusCode.OK)
                        get { content }.isNotNull()
                    }

                    val processingResponse = objectMapper.readValue(response.content, ResultsProcessing::class.java)

                    val now = LocalDateTime.now()

                    expectThat(processingResponse)
                        .isNotNull()
                        .and {
                            get { status }.isEqualTo(ResultsProcessingStatus.SUCCESS)
                            get { errorMessage }.isNull()
                            get { createdTimestamp }.isNotNull()
                                .and {
                                    get { year }.isEqualTo(now.year)
                                    get { month }.isEqualTo(now.month)
                                    get { dayOfMonth }.isEqualTo(now.dayOfMonth)
                                    get { hour }.isEqualTo(now.hour)
                                    get { minute }.isEqualTo(now.minute)
                                }
                        }
                }
        }
    }

    @Test
    fun `when trying to fetch processing results status for ID that does not exist should return 404 response`() {
        val publicId = "doesNotExist"

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/results/$publicId/status")
                .apply {
                    expectThat(response) {
                        get { status() }.isEqualTo(HttpStatusCode.NotFound)
                    }
                }
        }
    }
}
