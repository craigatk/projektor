package projektor.incomingresults

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.awaitility.kotlin.untilNotNull
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.server.api.results.ResultsProcessingStatus
import projektor.server.api.results.SaveResultsError
import projektor.server.api.results.SaveResultsResponse
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

class SaveGroupedResultsErrorApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `when results fail to parse should return error response code`() =
        testSuspend {
            val malformedResults = GroupedResultsXmlLoader().passingGroupedResults().replace("testsuite", "")

            val response =
                testClient.post("/groupedResults") {
                    headers {
                        append(HttpHeaders.ContentType, "application/json")
                    }
                    setBody(malformedResults)
                }
            expectThat(response.status).isEqualTo(HttpStatusCode.BadRequest)

            val resultsResponse = objectMapper.readValue(response.bodyAsText(), SaveResultsError::class.java)

            val publicId = resultsResponse.id
            assertNotNull(publicId)

            expectThat(resultsResponse.errorMessage).contains("(code 32) in content after '<' (malformed start element?)")

            await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.ERROR.name }
        }

    @Test
    fun `should still process results after multiple failures`() =
        testSuspend {
            val malformedResults = GroupedResultsXmlLoader().passingGroupedResults().replace("testsuite", "")
            val successfulResults = GroupedResultsXmlLoader().passingGroupedResults()

            (1..10).forEach { _ ->
                val response =
                    testClient.post("/groupedResults") {
                        headers {
                            append(HttpHeaders.ContentType, "application/json")
                        }
                        setBody(malformedResults)
                    }
                expectThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
                val resultsResponse = objectMapper.readValue(response.bodyAsText(), SaveResultsError::class.java)

                val publicId = resultsResponse.id
                assertNotNull(publicId)

                await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.ERROR.name }

                val resultsProcessingFailure = await untilNotNull { resultsProcessingFailureDao.fetchOneByPublicId(publicId) }
                expectThat(resultsProcessingFailure.body).isEqualTo(malformedResults)
            }

            (1..10).forEach { _ ->
                val response = postGroupedResultsJSON(successfulResults)

                val resultsResponse = objectMapper.readValue(response.bodyAsText(), SaveResultsResponse::class.java)

                val publicId = resultsResponse.id
                assertNotNull(publicId)

                await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.SUCCESS.name }
            }
        }

    @Test
    fun `when empty results body should respond with 400`() =
        testSuspend {
            val response = testClient.post("/groupedResults")
            expectThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
        }
}
