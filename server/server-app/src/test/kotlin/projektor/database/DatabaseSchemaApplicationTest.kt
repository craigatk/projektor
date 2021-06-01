package projektor.database

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.jooq.exception.DataAccessException
import org.junit.Test
import projektor.ApplicationTestCase
import projektor.server.api.results.SaveResultsResponse
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isNotNull
import kotlin.test.assertNotNull

class DatabaseSchemaApplicationTest : ApplicationTestCase() {
    @Test
    fun `should support saving results to a different schema`() {
        val requestBody = resultsXmlLoader.passing()

        databaseSchema = "projektor"

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/results") {
                dslContext.execute("CREATE SCHEMA IF NOT EXISTS projektor;")

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

                try {
                    await until {
                        val results =
                            dslContext.resultQuery("select id from public.test_run where public_id = {0}", publicId)
                                .fetch()
                        results.isEmpty()
                    }
                } catch (e: DataAccessException) {
                    expectThat(e.message).isNotNull().contains("""relation "public.test_run" does not exist""")
                }
            }
        }
    }
}
