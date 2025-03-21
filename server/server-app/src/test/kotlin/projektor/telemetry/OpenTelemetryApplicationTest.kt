package projektor.telemetry

import io.ktor.client.request.*
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class OpenTelemetryApplicationTest : ApplicationTestCase() {
    @Test
    fun `should add custom spans when fetching test run`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            testRunDBGenerator.createTestRun(
                publicId,
                listOf(
                    TestSuiteData(
                        "testSuite1",
                        listOf("testSuite1TestCase1", "testSuite1TestCase2"),
                        listOf(),
                        listOf(),
                    ),
                ),
            )

            val response = client.get("/run/$publicId")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val finishedSpans = exporter.finishedSpanItems

            expectThat(finishedSpans).hasSize(2)
                .any {
                    get { name }.isEqualTo("projektor.fetchTestRun")
                }
                .any {
                    get { name }.isEqualTo("projektor.fetchTestRun.mapper")
                }
        }
}
