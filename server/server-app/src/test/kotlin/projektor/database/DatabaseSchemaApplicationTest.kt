package projektor.database

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlin.test.assertNotNull
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.Test
import projektor.ApplicationTestCase
import projektor.server.api.SaveResultsResponse

class DatabaseSchemaApplicationTest : ApplicationTestCase() {
    @Test
    fun shouldParseRequestAndSaveResultsForPassingTest() {
        val requestBody = resultsXmlLoader.passing()

        databaseSchema = "projektor"

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/results") {
                dslContext.execute("CREATE SCHEMA IF NOT EXISTS projektor")

                addHeader(HttpHeaders.ContentType, "text/plain")
                setBody(requestBody)
            }.apply {
                val resultsResponse = objectMapper.readValue(response.content, SaveResultsResponse::class.java)

                val publicId = resultsResponse.id
                assertNotNull(publicId)

                await until {
                    val results = dslContext.resultQuery("select id from projektor.test_run where public_id = {0}", publicId).fetch()
                    results.isNotEmpty
                }

                await until {
                    val results = dslContext.resultQuery("select id from public.test_run where public_id = {0}", publicId).fetch()
                    results.isEmpty()
                }
            }
        }
    }
}
