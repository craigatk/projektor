package projektor.processing

import io.ktor.server.config.*
import projektor.parser.grouped.GroupedResultsParser

data class ProcessingConfig(val maxPayloadSize: Int) {
    companion object {
        fun createProcessingConfig(applicationConfig: ApplicationConfig): ProcessingConfig {
            val maxPayloadSize: Int = applicationConfig.propertyOrNull("ktor.processing.maxPayloadSize")?.toString()?.toInt() ?: GroupedResultsParser.DEFAULT_MAX_PAYLOAD_SIZE

            return ProcessingConfig(maxPayloadSize)
        }
    }
}
