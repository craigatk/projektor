package projektor.incomingresults

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
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

@KtorExperimentalAPI
class SaveResultsErrorApplicationTest : ApplicationTestCase() {
    @Test
    fun `should still process results after multiple failures`() {
        val malformedResults = resultsXmlLoader.passing().replace("testsuite", "")
        val successfulResults = resultsXmlLoader.passing()

        withTestApplication(::createTestApplication) {
            (1..10).forEach { _ ->
                handleRequest(HttpMethod.Post, "/results") {
                    addHeader(HttpHeaders.ContentType, "application/json")
                    setBody(malformedResults)
                }.apply {
                    val resultsResponse = objectMapper.readValue(response.content, SaveResultsResponse::class.java)

                    val publicId = resultsResponse.id
                    assertNotNull(publicId)

                    await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.ERROR.name }

                    val resultsProcessingFailure = await untilNotNull { resultsProcessingFailureDao.fetchOneByPublicId(publicId) }
                    expectThat(resultsProcessingFailure.body).isEqualTo(malformedResults)
                }
            }

            (1..10).forEach { _ ->
                handleRequest(HttpMethod.Post, "/results") {
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
}
