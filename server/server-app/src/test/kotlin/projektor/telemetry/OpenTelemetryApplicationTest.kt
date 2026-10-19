package projektor.telemetry

import io.ktor.client.request.*
import io.ktor.http.HttpStatusCode
import io.opentelemetry.api.common.AttributeKey
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo

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

            expectThat(finishedSpans)
                .any {
                    get { name }.isEqualTo("projektor.fetchTestRun")
                }
                .any {
                    get { name }.isEqualTo("projektor.fetchTestRun.mapper")
                }

            val repositorySpan = finishedSpans.first { it.name == "projektor.TestRunRepository.fetchTestRun" }
            expectThat(repositorySpan.attributes.get(AttributeKey.stringKey("projektor.persistence.backend"))).isEqualTo("database")
            expectThat(repositorySpan.attributes.get(AttributeKey.stringKey("projektor.id"))).isEqualTo(publicId.id)

            val querySpan = finishedSpans.first { it.name == "projektor.fetchTestRun" }
            expectThat(querySpan.parentSpanId).isEqualTo(repositorySpan.spanId)
        }

    @Test
    fun `should record repository call metrics`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            testRunDBGenerator.createSimpleTestRun(publicId)

            val response = client.get("/run/$publicId")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val timer =
                meterRegistry.find("persistence_repository_call")
                    .tags("backend", "database", "repository", "TestRunRepository", "method", "fetchTestRun", "outcome", "success")
                    .timer()
            expectThat(timer?.count() ?: 0L).isGreaterThanOrEqualTo(1L)
        }
}
