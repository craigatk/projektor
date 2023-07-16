package projektor.telemetry

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
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
    fun `should add custom spans when fetching test run`() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId") {
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(
                        TestSuiteData(
                            "testSuite1",
                            listOf("testSuite1TestCase1", "testSuite1TestCase2"),
                            listOf(),
                            listOf()
                        )
                    )
                )
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

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
    }
}
