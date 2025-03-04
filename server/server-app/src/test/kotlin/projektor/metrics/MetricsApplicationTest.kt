package projektor.metrics

import io.ktor.client.request.*
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.ApplicationTestCaseConfig
import projektor.incomingresults.randomPublicId

class MetricsApplicationTest : ApplicationTestCase() {
    @BeforeEach
    fun reset() {
        metricsStubber.reset()
    }

    @Test
    fun `when metrics enabled should publish to InfluxDB`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                metricsEnabled = true,
                metricsPort = metricsStubber.port(),
            ),
        ) {
            val publicId = randomPublicId()
            testRunDBGenerator.createTestRun(
                publicId,
                listOf(),
            )

            client.get("/run/$publicId")

            await until { metricsStubber.findCreateMetricsDatabaseRequests().isNotEmpty() }
            await until { metricsStubber.findWriteMetricsRequests().isNotEmpty() }
        }

    @Test
    fun `when metrics enabled with auth should publish to InfluxDB`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                metricsEnabled = true,
                metricsUsername = "metricsuser",
                metricsPassword = "metricspass",
                metricsPort = metricsStubber.port(),
            ),
        ) {
            val publicId = randomPublicId()

            testRunDBGenerator.createTestRun(
                publicId,
                listOf(),
            )

            client.get("/run/$publicId")

            await until { metricsStubber.findCreateMetricsDatabaseRequests().isNotEmpty() }
            await until { metricsStubber.findWriteMetricsRequests().isNotEmpty() }
        }

    companion object {
        private val metricsStubber = MetricsStubber()

        @BeforeAll
        @JvmStatic
        fun start() {
            metricsStubber.start()
        }

        @AfterAll
        @JvmStatic
        fun stop() {
            metricsStubber.stop()
        }
    }
}
