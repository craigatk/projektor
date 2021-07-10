package projektor.telemetry

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context

fun Tracer.startSpanWithParent(spanName: String): Span =
    this.spanBuilder(spanName)
        .setParent(Context.current().with(Span.current()))
        .startSpan()
