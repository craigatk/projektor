package projektor.incomingresults.auth

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.auth.AuthConfig
import projektor.server.api.results.SaveResultsResponse
import strikt.api.expectThat
import strikt.assertions.*
import kotlin.test.assertNotNull

@KtorExperimentalAPI
class SaveResultsTokenApplicationTest : ApplicationTestCase() {
    @Test
    fun `when token set and valid token included in header should save results`() {
        val requestBody = resultsXmlLoader.passing()

        val validPublishToken = "publish12345"
        publishToken = validPublishToken

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/results") {
                addHeader(HttpHeaders.ContentType, "text/plain")
                addHeader(AuthConfig.PublishToken, validPublishToken)
                setBody(requestBody)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val resultsResponse = objectMapper.readValue(response.content, SaveResultsResponse::class.java)

                val publicId = resultsResponse.id
                assertNotNull(publicId)
                expectThat(resultsResponse.uri).isEqualTo("/tests/$publicId")
            }
        }
    }

    @Test
    fun `when token set and invalid token included in header should return 401`() {
        val requestBody = resultsXmlLoader.passing()

        val validPublishToken = "publish12345"
        publishToken = validPublishToken

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/results") {
                addHeader(HttpHeaders.ContentType, "text/plain")
                addHeader(AuthConfig.PublishToken, "notPublish12345")
                setBody(requestBody)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.Unauthorized)
            }
        }
    }
}
