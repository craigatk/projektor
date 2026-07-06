package projektor.logging

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.encoder.Encoder
import ch.qos.logback.core.encoder.EncoderBase
import net.logstash.logback.encoder.LogstashEncoder

/**
 * Chooses between JSON and plain-text console encoding based on the JSON_LOGGER
 * env var at startup. This picks the delegate in Kotlin rather than via logback.xml's
 * <if>/<condition> tags because logback 1.5.37 broke that conditional wiring:
 * ByPropertiesConditionModelHandler pushes the evaluated branch state after <then>/<else>
 * have already been processed, so the condition result is never applied.
 */
class ConditionalConsoleEncoder : EncoderBase<ILoggingEvent>() {
    private lateinit var delegate: Encoder<ILoggingEvent>

    override fun start() {
        delegate =
            if (!System.getenv("JSON_LOGGER").isNullOrEmpty()) {
                LogstashEncoder().apply {
                    context = this@ConditionalConsoleEncoder.context
                    addProvider(TimeMillisJsonLoggingProvider())
                }
            } else {
                PatternLayoutEncoder().apply {
                    context = this@ConditionalConsoleEncoder.context
                    pattern = "%d{YYYY-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
                }
            }
        delegate.start()
        super.start()
    }

    override fun stop() {
        if (::delegate.isInitialized) {
            delegate.stop()
        }
        super.stop()
    }

    override fun headerBytes(): ByteArray? = delegate.headerBytes()

    override fun encode(event: ILoggingEvent): ByteArray = delegate.encode(event)

    override fun footerBytes(): ByteArray? = delegate.footerBytes()
}
