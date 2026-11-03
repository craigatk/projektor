package projektor.processing

import io.ktor.server.config.*
import projektor.parser.grouped.GroupedResultsParser
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class ProcessingConfig(
    val maxPayloadSize: Int,
    // How long shutting down waits for accepted uploads to finish processing. Should be
    // shorter than the time the server's host waits after asking it to stop before killing it.
    val shutdownTimeout: Duration = DEFAULT_SHUTDOWN_TIMEOUT,
) {
    companion object {
        val DEFAULT_SHUTDOWN_TIMEOUT: Duration = 25.seconds

        fun createProcessingConfig(applicationConfig: ApplicationConfig): ProcessingConfig {
            val maxPayloadSize: Int =
                applicationConfig.propertyOrNull(
                    "ktor.processing.maxPayloadSize",
                )?.getString()?.toInt() ?: GroupedResultsParser.DEFAULT_MAX_PAYLOAD_SIZE

            val shutdownTimeout: Duration =
                applicationConfig.propertyOrNull(
                    "ktor.processing.shutdownTimeoutSeconds",
                )?.getString()?.toLong()?.seconds ?: DEFAULT_SHUTDOWN_TIMEOUT

            return ProcessingConfig(maxPayloadSize, shutdownTimeout)
        }
    }
}
