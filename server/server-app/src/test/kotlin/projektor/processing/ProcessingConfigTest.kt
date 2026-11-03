package projektor.processing

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import org.junit.jupiter.api.Test
import projektor.parser.grouped.GroupedResultsParser
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.time.Duration.Companion.seconds

class ProcessingConfigTest {
    @Test
    fun `should read configured values`() {
        val applicationConfig =
            HoconApplicationConfig(
                ConfigFactory.parseString(
                    "ktor.processing { maxPayloadSize = 60000000, shutdownTimeoutSeconds = 10 }",
                ),
            )

        val processingConfig = ProcessingConfig.createProcessingConfig(applicationConfig)

        expectThat(processingConfig.maxPayloadSize).isEqualTo(60_000_000)
        expectThat(processingConfig.shutdownTimeout).isEqualTo(10.seconds)
    }

    @Test
    fun `should use defaults when not configured`() {
        val processingConfig = ProcessingConfig.createProcessingConfig(HoconApplicationConfig(ConfigFactory.empty()))

        expectThat(processingConfig.maxPayloadSize).isEqualTo(GroupedResultsParser.DEFAULT_MAX_PAYLOAD_SIZE)
        expectThat(processingConfig.shutdownTimeout).isEqualTo(ProcessingConfig.DEFAULT_SHUTDOWN_TIMEOUT)
    }
}
