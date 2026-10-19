package projektor.objectstore

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer

/**
 * Records a timer and a span for each request the object store client sends, and counts
 * compare-and-swap conflicts in JsonObjectStore.
 *
 * Metrics:
 * - `object_store_request` timer, tagged with `operation`, `bucket` and `outcome`
 *   (`success`, `not_found`, `precondition_failed` or `error`)
 * - `object_store_update_conflict` counter, tagged with `bucket`: a conditional update lost to
 *   another writer and was retried
 * - `object_store_update_exhausted` counter, tagged with `bucket`: an update gave up after its max attempts
 */
class ObjectStoreTelemetry(
    private val meterRegistry: MeterRegistry = Metrics.globalRegistry,
    private val tracer: Tracer = GlobalOpenTelemetry.getTracer("projektor.objectstore"),
) {
    fun <T> record(
        operation: String,
        bucketName: String,
        objectName: String?,
        outcomeOf: (T) -> String = { SUCCESS },
        block: () -> T,
    ): T {
        val spanBuilder =
            tracer.spanBuilder("projektor.objectStore.$operation")
                .setSpanKind(SpanKind.CLIENT)
                .setAttribute(BUCKET_ATTRIBUTE, bucketName)
        objectName?.let { spanBuilder.setAttribute(KEY_ATTRIBUTE, it) }
        val span = spanBuilder.startSpan()

        val sample = Timer.start(meterRegistry)
        var outcome = ERROR

        try {
            val result = span.makeCurrent().use { block() }
            outcome = outcomeOf(result)
            return result
        } catch (e: PreconditionFailedException) {
            // An expected result of a conditional write, the caller decides whether it's an error
            outcome = PRECONDITION_FAILED
            throw e
        } catch (e: Throwable) {
            span.recordException(e)
            span.setStatus(StatusCode.ERROR)
            throw e
        } finally {
            span.setAttribute(OUTCOME_ATTRIBUTE, outcome)
            span.end()
            sample.stop(
                meterRegistry.timer(REQUEST_TIMER, "operation", operation, "bucket", bucketName, "outcome", outcome),
            )
        }
    }

    fun recordUpdateConflict(bucketName: String) {
        meterRegistry.counter(UPDATE_CONFLICT_COUNTER, "bucket", bucketName).increment()
    }

    fun recordUpdateExhausted(bucketName: String) {
        meterRegistry.counter(UPDATE_EXHAUSTED_COUNTER, "bucket", bucketName).increment()
    }

    companion object {
        const val REQUEST_TIMER = "object_store_request"
        const val UPDATE_CONFLICT_COUNTER = "object_store_update_conflict"
        const val UPDATE_EXHAUSTED_COUNTER = "object_store_update_exhausted"

        const val SUCCESS = "success"
        const val NOT_FOUND = "not_found"
        const val PRECONDITION_FAILED = "precondition_failed"
        const val ERROR = "error"

        private const val BUCKET_ATTRIBUTE = "projektor.object_store.bucket"
        private const val KEY_ATTRIBUTE = "projektor.object_store.key"
        private const val OUTCOME_ATTRIBUTE = "projektor.object_store.outcome"
    }
}
