package projektor.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import com.fasterxml.jackson.core.JsonGenerator
import net.logstash.logback.composite.AbstractFieldJsonProvider
import net.logstash.logback.composite.JsonWritingUtils

class TimeMillisJsonLoggingProvider : AbstractFieldJsonProvider<ILoggingEvent> {
    constructor() {
        fieldName = FIELD_NAME
    }

    override fun writeTo(
        generator: JsonGenerator,
        event: ILoggingEvent,
    ) {
        JsonWritingUtils.writeNumberField(generator, fieldName, event.timeStamp / 1000)
    }

    companion object {
        const val FIELD_NAME = "timeMillis"
    }
}
