package projektor.persistence

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import projektor.server.api.PublicId
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isNotEqualTo
import java.io.IOException
import java.time.Duration

interface ExampleRepository {
    suspend fun fetchName(publicId: PublicId): String?

    suspend fun fetchNameAfterDelay(publicId: PublicId): String

    suspend fun fetchOnIODispatcher(): String

    suspend fun save(name: String)

    suspend fun failImmediately()

    suspend fun failWithCheckedException()

    suspend fun failAfterDelay()

    fun countNames(): Int
}

class ExampleRepositoryImpl : ExampleRepository {
    val savedNames = mutableListOf<String>()

    var spanIdDuringCall: String? = null

    override suspend fun fetchName(publicId: PublicId): String? = if (publicId.id == "missing") null else "name-${publicId.id}"

    override suspend fun fetchNameAfterDelay(publicId: PublicId): String {
        delay(10)
        spanIdDuringCall = Span.current().spanContext.spanId
        return "delayed-${publicId.id}"
    }

    override suspend fun fetchOnIODispatcher(): String =
        withContext(Dispatchers.IO) {
            spanIdDuringCall = Span.current().spanContext.spanId
            "from IO"
        }

    override suspend fun save(name: String) {
        savedNames.add(name)
    }

    override suspend fun failImmediately() {
        throw IllegalStateException("failed immediately")
    }

    override suspend fun failWithCheckedException() {
        throw IOException("checked failure")
    }

    override suspend fun failAfterDelay() {
        delay(10)
        throw IllegalArgumentException("failed after delay")
    }

    override fun countNames(): Int = savedNames.size
}

class PersistenceInstrumentationTest {
    private val exporter = InMemorySpanExporter.create()
    private val tracer =
        SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(exporter))
            .build()
            .get("test")

    private val meterRegistry = SimpleMeterRegistry()

    private val repositoryImpl = ExampleRepositoryImpl()

    private val repository: ExampleRepository =
        PersistenceInstrumentation(PersistenceType.OBJECT_STORE, meterRegistry, Duration.ofSeconds(1), tracer)
            .instrument<ExampleRepository>(repositoryImpl)

    private fun callTimer(
        method: String,
        outcome: String,
    ) = meterRegistry.find(PersistenceInstrumentation.CALL_TIMER)
        .tags("backend", "object-store", "repository", "ExampleRepository", "method", method, "outcome", outcome)
        .timer()

    @Test
    fun `should pass through results of suspend functions that complete without suspending`(): Unit =
        runBlocking {
            expectThat(repository.fetchName(PublicId("abc"))).isEqualTo("name-abc")
            expectThat(repository.fetchName(PublicId("missing"))).isEqualTo(null)

            repository.save("saved")
            expectThat(repositoryImpl.savedNames).isEqualTo(mutableListOf("saved"))

            expectThat(callTimer("fetchName", "success")?.count()).isEqualTo(2L)
            expectThat(callTimer("save", "success")?.count()).isEqualTo(1L)
        }

    @Test
    fun `should time suspend functions until they complete`(): Unit =
        runBlocking {
            expectThat(repository.fetchNameAfterDelay(PublicId("abc"))).isEqualTo("delayed-abc")

            val timer = callTimer("fetchNameAfterDelay", "success")
            expectThat(timer?.count()).isEqualTo(1L)
            expectThat(timer!!.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(10.0)
        }

    @Test
    fun `should make the repository span current while the call runs`(): Unit =
        runBlocking {
            repository.fetchNameAfterDelay(PublicId("abc"))
            val spanIdAfterDelay = repositoryImpl.spanIdDuringCall

            repository.fetchOnIODispatcher()
            val spanIdOnIO = repositoryImpl.spanIdDuringCall

            val spans = exporter.finishedSpanItems
            expectThat(spans.map { it.name })
                .containsExactlyInAnyOrder(
                    "projektor.ExampleRepository.fetchNameAfterDelay",
                    "projektor.ExampleRepository.fetchOnIODispatcher",
                )
            expectThat(spanIdAfterDelay).isEqualTo(spans.first { it.name.endsWith("fetchNameAfterDelay") }.spanId)
            expectThat(spanIdOnIO).isEqualTo(spans.first { it.name.endsWith("fetchOnIODispatcher") }.spanId)

            val span = spans.first { it.name.endsWith("fetchNameAfterDelay") }
            expectThat(span.attributes.get(AttributeKey.stringKey("projektor.id"))).isEqualTo("abc")
            expectThat(span.attributes.get(AttributeKey.stringKey("projektor.persistence.backend"))).isEqualTo("object-store")
        }

    @Test
    fun `should nest repository spans under the caller's span`(): Unit =
        runBlocking {
            val parentSpan = tracer.spanBuilder("parent").startSpan()
            parentSpan.makeCurrent().use {
                repository.fetchName(PublicId("abc"))
            }
            parentSpan.end()

            val repositorySpan = exporter.finishedSpanItems.first { it.name == "projektor.ExampleRepository.fetchName" }
            expectThat(repositorySpan.parentSpanId).isEqualTo(parentSpan.spanContext.spanId)
        }

    @Test
    fun `should rethrow exceptions and record them as errors`(): Unit =
        runBlocking {
            expectThrows<IllegalStateException> { repository.failImmediately() }
            expectThrows<IllegalArgumentException> { repository.failAfterDelay() }

            expectThat(callTimer("failImmediately", "error")?.count()).isEqualTo(1L)
            expectThat(callTimer("failAfterDelay", "error")?.count()).isEqualTo(1L)

            val span = exporter.finishedSpanItems.first { it.name == "projektor.ExampleRepository.failImmediately" }
            expectThat(span.status.statusCode).isEqualTo(StatusCode.ERROR)
            expectThat(span.events.map { it.name }).isEqualTo(listOf("exception"))
        }

    @Test
    fun `should rethrow checked exceptions without wrapping them`(): Unit =
        runBlocking {
            expectThrows<IOException> { repository.failWithCheckedException() }
                .get { message }
                .isEqualTo("checked failure")

            expectThat(callTimer("failWithCheckedException", "error")?.count()).isEqualTo(1L)
        }

    @Test
    fun `should instrument non-suspend functions`() {
        runBlocking { repository.save("one") }

        expectThat(repository.countNames()).isEqualTo(1)
        expectThat(callTimer("countNames", "success")?.count()).isEqualTo(1L)
    }

    @Test
    fun `should not instrument equals, hashCode and toString`() {
        expectThat(repository.toString()).isEqualTo(repositoryImpl.toString())
        expectThat(repository.hashCode()).isEqualTo(repositoryImpl.hashCode())
        expectThat(
            repository,
        ).isNotEqualTo(
            PersistenceInstrumentation(PersistenceType.DATABASE, meterRegistry).instrument<ExampleRepository>(ExampleRepositoryImpl()),
        )

        expectThat(meterRegistry.find(PersistenceInstrumentation.CALL_TIMER).timers().size).isEqualTo(0)
    }

    @Test
    fun `should record concurrent calls separately`(): Unit =
        runBlocking {
            val names =
                (1..20).map { idx ->
                    CoroutineScope(Dispatchers.IO).async { repository.fetchNameAfterDelay(PublicId("id$idx")) }
                }.awaitAll()

            expectThat(names.toSet().size).isEqualTo(20)
            expectThat(callTimer("fetchNameAfterDelay", "success")?.count()).isEqualTo(20L)
            expectThat(exporter.finishedSpanItems.map { it.spanId }.toSet().size).isEqualTo(20)
        }
}
