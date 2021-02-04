package projektor.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.LoggingEvent
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import net.logstash.logback.encoder.LogstashEncoder
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

class TimeMillisJsonLoggingProviderTest {
    private val objectMapper = ObjectMapper()

    @Test
    fun `should write timeMillis field with Unix epoch value`() {
        val encoder = LogstashEncoder()
        encoder.addProvider(TimeMillisJsonLoggingProvider())

        encoder.start()

        val logger = LoggerContext().getLogger("MyLogger")

        val event = LoggingEvent("MyClass", logger, Level.INFO, "The message", null, null)
        event.timeStamp = 12345000

        val encoded = encoder.encode(event)

        val node: JsonNode = objectMapper.readTree(encoded)

        val timeMillisNode = node.get("timeMillis")

        expectThat(timeMillisNode).isNotNull()
        expectThat(timeMillisNode.isNumber).isTrue()
        expectThat(timeMillisNode.asLong()).isEqualTo(12345L)
    }
}
