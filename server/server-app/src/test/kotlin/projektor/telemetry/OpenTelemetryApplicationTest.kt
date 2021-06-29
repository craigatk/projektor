package projektor.telemetry

import io.ktor.http.*
import io.ktor.server.testing.*
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class OpenTelemetryApplicationTest : ApplicationTestCase() {
    private val exporter = InMemorySpanExporter.create()
    private val tracerProvider = SdkTracerProvider
        .builder()
        .addSpanProcessor(SimpleSpanProcessor.create(exporter))
        .build()

    @Test
    fun `should add custom spans when fetching test run`() {
        // Needed when running the full test suite as the global telemetry
        // instance may be set by another test and it can only be set once.
        GlobalOpenTelemetry.resetForTest()

        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal()

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
