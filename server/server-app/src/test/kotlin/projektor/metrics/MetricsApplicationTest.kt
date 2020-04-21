package projektor.metrics

import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId

@KtorExperimentalAPI
class MetricsApplicationTest : ApplicationTestCase() {

    private val metricsStubber = MetricsStubber()

    @BeforeEach
    fun start() {
        metricsStubber.start()
    }

    @AfterEach
    fun stop() {
        metricsStubber.stop()
    }

    @Test
    fun `when metrics enabled should publish to InfluxDB`() {
        metricsEnabled = true
        metricsPort = metricsStubber.port()

        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId") {
                testRunDBGenerator.createTestRun(
                        publicId,
                        listOf()
                )
            }.apply {
                await until { metricsStubber.findCreateMetricsDatabaseRequests().isNotEmpty() }
                await until { metricsStubber.findWriteMetricsRequests().isNotEmpty() }
            }
        }
    }

    @Test
    fun `when metrics enabled with auth should publish to InfluxDB`() {
        metricsEnabled = true
        metricsUsername = "metricsuser"
        metricsPassword = "metricspass"
        metricsPort = metricsStubber.port()

        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId") {
                testRunDBGenerator.createTestRun(
                        publicId,
                        listOf()
                )
            }.apply {
                await until { metricsStubber.findCreateMetricsDatabaseRequests().isNotEmpty() }
                await until { metricsStubber.findWriteMetricsRequests().isNotEmpty() }
            }
        }
    }
}
