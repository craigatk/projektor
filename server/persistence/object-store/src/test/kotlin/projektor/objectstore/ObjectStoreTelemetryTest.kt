package projektor.objectstore

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import projektor.objectstore.json.ConcurrentUpdateException
import projektor.objectstore.json.JsonObjectStore
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ObjectStoreTelemetryTest {
    private val config = ObjectStoreConfig("http://localhost:9000", "minio_access_key", "minio_secret_key")

    private val exporter = InMemorySpanExporter.create()
    private val tracer =
        SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(exporter))
            .build()
            .get("test")

    private val meterRegistry = SimpleMeterRegistry()
    private val client = ObjectStoreClient(config, ObjectStoreTelemetry(meterRegistry, tracer))

    private val bucketName = "telemetrybucket"

    // Unique per run so tests don't see objects left over from previous runs
    private val prefix = "test-${UUID.randomUUID()}/"

    @BeforeAll
    fun createBucket() {
        client.createBucketIfNotExists(bucketName)
    }

    @AfterAll
    fun removeObjects() {
        client.removeObjectsWithPrefix(bucketName, prefix)
    }

    private fun requestCount(
        operation: String,
        outcome: String,
    ) = meterRegistry.find(ObjectStoreTelemetry.REQUEST_TIMER)
        .tags("operation", operation, "bucket", bucketName, "outcome", outcome)
        .timer()
        ?.count() ?: 0L

    @Test
    fun `should record put and get requests`() {
        val objectName = "${prefix}put-get"
        exporter.reset()
        val putsBefore = requestCount("put", ObjectStoreTelemetry.SUCCESS)
        val getsBefore = requestCount("get", ObjectStoreTelemetry.SUCCESS)

        client.putObject(bucketName, objectName, "contents".toByteArray(), "text/plain")
        expectThat(client.getObjectWithETag(bucketName, objectName)).isNotNull()

        expectThat(requestCount("put", ObjectStoreTelemetry.SUCCESS)).isEqualTo(putsBefore + 1)
        expectThat(requestCount("get", ObjectStoreTelemetry.SUCCESS)).isEqualTo(getsBefore + 1)

        val putSpan = exporter.finishedSpanItems.first { it.name == "projektor.objectStore.put" }
        expectThat(putSpan.attributes.get(AttributeKey.stringKey("projektor.object_store.key"))).isEqualTo(objectName)
        expectThat(putSpan.attributes.get(AttributeKey.stringKey("projektor.object_store.bucket"))).isEqualTo(bucketName)
        expectThat(putSpan.status.statusCode).isEqualTo(StatusCode.UNSET)
    }

    @Test
    fun `should record missing objects as not found`() {
        val notFoundBefore = requestCount("get", ObjectStoreTelemetry.NOT_FOUND)

        expectThat(client.getObjectWithETag(bucketName, "${prefix}does-not-exist")).isNull()

        expectThat(requestCount("get", ObjectStoreTelemetry.NOT_FOUND)).isEqualTo(notFoundBefore + 1)
    }

    @Test
    fun `should record failed conditional writes without marking the span as an error`() {
        val objectName = "${prefix}conditional"
        client.putObject(bucketName, objectName, "first".toByteArray(), "text/plain")
        exporter.reset()
        val failedBefore = requestCount("put", ObjectStoreTelemetry.PRECONDITION_FAILED)

        expectThrows<PreconditionFailedException> {
            client.putObject(bucketName, objectName, "second".toByteArray(), "text/plain", WriteCondition.IfNotExists)
        }

        expectThat(requestCount("put", ObjectStoreTelemetry.PRECONDITION_FAILED)).isEqualTo(failedBefore + 1)

        val putSpan = exporter.finishedSpanItems.first { it.name == "projektor.objectStore.put" }
        expectThat(putSpan.attributes.get(AttributeKey.stringKey("projektor.object_store.outcome")))
            .isEqualTo(ObjectStoreTelemetry.PRECONDITION_FAILED)
        expectThat(putSpan.status.statusCode).isEqualTo(StatusCode.UNSET)
    }

    @Test
    fun `should count update conflicts and exhausted updates`() {
        val store = JsonObjectStore(client, bucketName, prefix)
        val impatientStore = JsonObjectStore(client, bucketName, prefix, maxUpdateAttempts = 3)
        store.write("conflicts.json", mapOf("count" to 0))
        var transformCalls = 0

        expectThrows<ConcurrentUpdateException> {
            impatientStore.update<Map<*, *>>("conflicts.json") { current ->
                transformCalls++
                // Another writer sneaks in between every read and write
                store.write("conflicts.json", mapOf("count" to transformCalls * 100))
                current
                    ?.plus("count" to transformCalls)
            }
        }

        expectThat(meterRegistry.counter(ObjectStoreTelemetry.UPDATE_CONFLICT_COUNTER, "bucket", bucketName).count()).isEqualTo(3.0)
        expectThat(meterRegistry.counter(ObjectStoreTelemetry.UPDATE_EXHAUSTED_COUNTER, "bucket", bucketName).count()).isEqualTo(1.0)
    }
}
