package projektor.objectstore

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import okio.ForwardingSource
import okio.buffer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import projektor.objectstore.json.JsonObjectStore
import projektor.objectstore.json.ObjectStorePermissionException
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.io.IOException
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

/**
 * Faults that only appear between the HTTP client and the store, injected inside MinIO's retry interceptor
 * so the client's real retries run against the real store.
 */
class ObjectStoreClientFaultTest {
    private val config = ObjectStoreConfig("http://localhost:9000", "minio_access_key", "minio_secret_key")
    private val bucketName = "faultbucket"
    private val keyPrefix = "fault-${UUID.randomUUID()}"

    private val plainClient = ObjectStoreClient(config)
    private val plainStore = JsonObjectStore(plainClient, bucketName, keyPrefix)

    private var fault: (Interceptor.Chain) -> Response? = { null }

    private val faultyClient =
        ObjectStoreClient(
            config,
            ObjectStoreTelemetry(),
            ObjectStoreClient.createHttpClient()
                .newBuilder()
                .addInterceptor { chain -> fault(chain) ?: chain.proceed(chain.request()) }
                .build(),
        )
    private val faultyStore = JsonObjectStore(faultyClient, bucketName, keyPrefix)

    data class Counter(val value: Int)

    @BeforeEach
    fun createBucket() {
        plainClient.createBucketIfNotExists(bucketName)
    }

    @AfterEach
    fun removeDocuments() {
        plainStore.deletePrefix("")
    }

    private fun badGatewayAfterCommittingNextPut() {
        var armed = true

        fault = { chain ->
            if (armed && chain.request().method == "PUT") {
                armed = false
                chain.proceed(chain.request()).close()
                response(chain, 502, "")
            } else {
                null
            }
        }
    }

    private fun response(
        chain: Interceptor.Chain,
        code: Int,
        body: String,
    ) = Response.Builder()
        .request(chain.request())
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message("Simulated")
        .body(body.toResponseBody("application/xml".toMediaType()))
        .build()

    @Test
    fun `creating a document should succeed when the client retries a write that committed`() {
        badGatewayAfterCommittingNextPut()

        expectThat(faultyStore.create("doc.json", Counter(1))).isNotNull()

        expectThat(plainStore.read<Counter>("doc.json")).isEqualTo(Counter(1))
    }

    @Test
    fun `updating a document should apply the change once when the client retries a write that committed`() {
        plainStore.write("doc.json", Counter(0))
        val transformRuns = AtomicInteger()

        badGatewayAfterCommittingNextPut()

        val updated =
            faultyStore.update<Counter>("doc.json") { current ->
                transformRuns.incrementAndGet()
                Counter(current!!.value + 1)
            }

        expectThat(updated).isEqualTo(Counter(1))
        expectThat(plainStore.read<Counter>("doc.json")).isEqualTo(Counter(1))
        expectThat(transformRuns.get()).isEqualTo(1)
    }

    @Test
    fun `another writer's document should still count as a conflict`() {
        plainStore.write("doc.json", Counter(0))
        val staleETag = plainStore.readVersioned("doc.json", Counter::class.java)!!.etag
        plainStore.write("doc.json", Counter(1))

        expectThat(plainStore.create("doc.json", Counter(2))).isNull()
        expectThat(plainStore.replace("doc.json", Counter(2), staleETag)).isNull()
        expectThat(plainStore.read<Counter>("doc.json")).isEqualTo(Counter(1))
    }

    @Test
    fun `removing objects should fail when the store reports that an object failed to delete`() {
        fault = { chain ->
            if (chain.request().method == "POST" && chain.request().url.queryParameterNames.contains("delete")) {
                response(
                    chain,
                    200,
                    """<DeleteResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">""" +
                        "<Error><Key>doc.json</Key><Code>AccessDenied</Code><Message>Denied</Message></Error></DeleteResult>",
                )
            } else {
                null
            }
        }

        expectThrows<ObjectDeletionException> { faultyClient.removeObjects(bucketName, listOf("doc.json")) }
    }

    @Test
    fun `permission check should pass and clean up after itself when everything is allowed`() {
        plainStore.verifyPermissions()

        expectThat(plainStore.list("").objectNames).isEqualTo(listOf())
    }

    @Test
    fun `permission check should fail when credentials can't read a missing object`() {
        denyWhen { chain -> chain.request().method == "GET" && chain.request().url.encodedPath.endsWith(".missing") }

        expectThrows<ObjectStorePermissionException> { faultyStore.verifyPermissions() }
    }

    @Test
    fun `permission check should fail when credentials can't list objects`() {
        denyWhen { chain -> chain.request().url.queryParameter("list-type") != null }

        expectThrows<ObjectStorePermissionException> { faultyStore.verifyPermissions() }
    }

    @Test
    fun `permission check should fail when credentials can't delete objects`() {
        denyWhen { chain -> chain.request().method == "DELETE" }

        expectThrows<ObjectStorePermissionException> { faultyStore.verifyPermissions() }
    }

    @Test
    fun `should retry a write when the connection fails before it's sent`() {
        failNextRequest("PUT") { throw IOException("Simulated connection failure") }

        faultyStore.write("doc.json", Counter(1))

        expectThat(plainStore.read<Counter>("doc.json")).isEqualTo(Counter(1))
    }

    @Test
    fun `should apply an update once when the connection fails after the write committed`() {
        plainStore.write("doc.json", Counter(0))
        val transformRuns = AtomicInteger()

        failNextRequest("PUT") { chain ->
            chain.proceed(chain.request()).close()
            throw IOException("Simulated connection failure")
        }

        val updated =
            faultyStore.update<Counter>("doc.json") { current ->
                transformRuns.incrementAndGet()
                Counter(current!!.value + 1)
            }

        expectThat(updated).isEqualTo(Counter(1))
        expectThat(plainStore.read<Counter>("doc.json")).isEqualTo(Counter(1))
        expectThat(transformRuns.get()).isEqualTo(1)
    }

    @Test
    fun `should retry a read when the connection fails partway through the response`() {
        plainStore.write("doc.json", Counter(1))

        failNextRequest("GET") { chain ->
            val response = chain.proceed(chain.request())
            val failingSource =
                object : ForwardingSource(response.body.source()) {
                    override fun read(
                        sink: Buffer,
                        byteCount: Long,
                    ): Long = throw IOException("Simulated connection failure")
                }

            response.newBuilder().body(failingSource.buffer().asResponseBody(null, -1)).build()
        }

        expectThat(faultyStore.read<Counter>("doc.json")).isEqualTo(Counter(1))
    }

    @Test
    fun `should retry listing and deleting when the connection fails`() {
        plainStore.write("doc.json", Counter(1))

        failNextRequest("GET") { throw IOException("Simulated connection failure") }
        expectThat(faultyStore.list("").objectNames).isEqualTo(listOf("doc.json"))

        failNextRequest("POST") { throw IOException("Simulated connection failure") }
        faultyStore.deleteAll(listOf("doc.json"))
        expectThat(plainStore.exists("doc.json")).isEqualTo(false)
    }

    @Test
    fun `should give up after repeated network failures`() {
        val attempts = AtomicInteger()

        fault = { chain ->
            if (chain.request().method == "PUT") {
                attempts.incrementAndGet()
                throw IOException("Simulated connection failure")
            }
            null
        }

        expectThrows<Exception> { faultyStore.write("doc.json", Counter(1)) }
        expectThat(attempts.get()).isEqualTo(3)
    }

    @Test
    fun `should not retry a response from the store as a network failure`() {
        val attempts = AtomicInteger()

        fault = { chain ->
            if (chain.request().method == "PUT") {
                attempts.incrementAndGet()
                response(chain, 403, "<Error><Code>AccessDenied</Code><Message>Denied</Message></Error>")
            } else {
                null
            }
        }

        expectThrows<Exception> { faultyStore.write("doc.json", Counter(1)) }
        expectThat(attempts.get()).isEqualTo(1)
    }

    // Leaves the requests that find the bucket's region alone, whose method is also GET
    private fun failNextRequest(
        method: String,
        failure: (Interceptor.Chain) -> Response,
    ) {
        var armed = true

        fault = { chain ->
            if (armed && chain.request().method == method && !chain.request().url.queryParameterNames.contains("location")) {
                armed = false
                failure(chain)
            } else {
                null
            }
        }
    }

    private fun denyWhen(predicate: (Interceptor.Chain) -> Boolean) {
        fault = { chain ->
            if (predicate(chain)) {
                response(chain, 403, "<Error><Code>AccessDenied</Code><Message>Denied</Message></Error>")
            } else {
                null
            }
        }
    }
}
