package projektor.incomingresults

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.assertNotNull
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.awaitility.kotlin.untilNotNull
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.server.api.results.ResultsProcessingStatus
import projektor.server.api.results.SaveResultsErrorResponse
import projektor.server.api.results.SaveResultsResponse
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@KtorExperimentalAPI
class SaveGroupedResultsErrorApplicationTest : ApplicationTestCase() {
    @Test
    fun `when results fail to parse should return error response code`() {
        val malformedResults = GroupedResultsXmlLoader().passingGroupedResults().replace("testsuite", "")

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(malformedResults)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                val resultsResponse = objectMapper.readValue(response.content, SaveResultsErrorResponse::class.java)

                val publicId = resultsResponse.id
                assertNotNull(publicId)

                await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.ERROR.name }
            }
        }
    }

    @Test
    fun `should still process results after multiple failures`() {
        val malformedResults = GroupedResultsXmlLoader().passingGroupedResults().replace("testsuite", "")
        val successfulResults = GroupedResultsXmlLoader().passingGroupedResults()

        withTestApplication(::createTestApplication) {
            (1..10).forEach { _ ->
                handleRequest(HttpMethod.Post, "/groupedResults") {
                    addHeader(HttpHeaders.ContentType, "application/json")
                    setBody(malformedResults)
                }.apply {
                    expectThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                    val resultsResponse = objectMapper.readValue(response.content, SaveResultsErrorResponse::class.java)

                    val publicId = resultsResponse.id
                    assertNotNull(publicId)

                    await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.ERROR.name }

                    val resultsProcessingFailure = await untilNotNull { resultsProcessingFailureDao.fetchOneByPublicId(publicId) }
                    expectThat(resultsProcessingFailure.body).isEqualTo(malformedResults)
                }
            }

            (1..10).forEach { _ ->
                handleRequest(HttpMethod.Post, "/groupedResults") {
                    addHeader(HttpHeaders.ContentType, "application/json")
                    setBody(successfulResults)
                }.apply {
                    val resultsResponse = objectMapper.readValue(response.content, SaveResultsResponse::class.java)

                    val publicId = resultsResponse.id
                    assertNotNull(publicId)

                    await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.SUCCESS.name }
                }
            }
        }
    }

    @Test
    fun `when empty results body should respond with 400`() {
        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
            }
        }
    }
}
