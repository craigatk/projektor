package projektor

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase

class TestLogAppender : AppenderBase<ILoggingEvent>() {
    private val logContentsBuilder = StringBuilder()

    override fun append(eventObject: ILoggingEvent?) {
        eventObject?.message.let { logContentsBuilder.append(it).append('\n') }
    }

    fun getLogContents() = logContentsBuilder.toString()
}
