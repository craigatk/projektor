package projektor.incomingresults

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import java.time.LocalDateTime
import kotlin.test.assertNotNull
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilNotNull
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.server.api.results.ResultsProcessing
import projektor.server.api.results.ResultsProcessingStatus
import projektor.server.api.results.SaveResultsResponse
import strikt.api.expectThat
import strikt.assertions.*

@KtorExperimentalAPI
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
