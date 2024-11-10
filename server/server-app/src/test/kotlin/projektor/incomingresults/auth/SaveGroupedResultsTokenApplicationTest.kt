package projektor.incomingresults.auth

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.auth.AuthConfig
import projektor.parser.GroupedResultsXmlLoader
import projektor.server.api.results.SaveResultsResponse
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

class SaveGroupedResultsTokenApplicationTest : ApplicationTestCase() {
    @Test
    fun `when token set and valid token included in header should save results`() =
        testSuspend {
            val requestBody = GroupedResultsXmlLoader().passingGroupedResults()

            val validPublishToken = "publish12345"
            publishToken = validPublishToken

            startTestServer()

            val postResponse =
                testClient.post("/groupedResults") {
                    headers {
                        append(HttpHeaders.ContentType, "text/plain")
                        append(AuthConfig.PUBLISH_TOKEN, validPublishToken)
                    }
                    setBody(requestBody)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            val resultsResponse = objectMapper.readValue(postResponse.bodyAsText(), SaveResultsResponse::class.java)

            val publicId = resultsResponse.id
            assertNotNull(publicId)
            expectThat(resultsResponse.uri).isEqualTo("/tests/$publicId")
        }

    @Test
    fun `when token set and invalid token included in header should return 401`() =
        testSuspend {
            val requestBody = GroupedResultsXmlLoader().passingGroupedResults()

            val validPublishToken = "publish12345"
            publishToken = validPublishToken

            startTestServer()

            val postResponse =
                testClient.post("/groupedResults") {
                    headers {
                        append(HttpHeaders.ContentType, "text/plain")
                        append(AuthConfig.PUBLISH_TOKEN, "notPublish12345")
                    }
                    setBody(requestBody)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.Unauthorized)
        }
}
