package projektor.database

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.jooq.exception.DataAccessException
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.ApplicationTestCaseConfig
import projektor.server.api.results.SaveResultsResponse
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import kotlin.test.assertNotNull

class DatabaseSchemaApplicationTest : ApplicationTestCase() {
    @Test
    fun `should support saving results to a different schema`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                databaseSchema = "projektor",
            ),
        ) {
            val requestBody = resultsXmlLoader.passing()

            dslContext.execute("CREATE SCHEMA IF NOT EXISTS projektor;")

            val postResponse =
                client.post("/results") {
                    headers {
                        append(HttpHeaders.ContentType, "text/plain")
                    }
                    setBody(requestBody)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            val resultsResponse = objectMapper.readValue(postResponse.bodyAsText(), SaveResultsResponse::class.java)

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
