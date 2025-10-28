package projektor.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import net.logstash.logback.composite.AbstractFieldJsonProvider
import net.logstash.logback.composite.JsonWritingUtils
import tools.jackson.core.JsonGenerator

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
