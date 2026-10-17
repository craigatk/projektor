package projektor.objectstore.json

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isLessThan
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue
import java.time.Instant
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

data class TestDocument(val name: String, val count: Int, val createdTimestamp: Instant? = null)

data class CounterDocument(val count: Int)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JsonObjectStoreTest {
    private val config = ObjectStoreConfig("http://localhost:9000", "minio_access_key", "minio_secret_key")
    private val client = ObjectStoreClient(config)

    private val bucketName = "jsonbucket"

    // Unique per run so tests don't see documents left over from previous runs
    private val keyPrefix = "test-${UUID.randomUUID()}"

    private val store = JsonObjectStore(client, bucketName, keyPrefix)

    @BeforeAll
    fun createBucket() {
        client.createBucketIfNotExists(bucketName)
    }

    @AfterAll
    fun removeDocuments() {
        store.deletePrefix("")
    }

    @Test
    fun `should write and read document`() {
        val document = TestDocument("doc", 3, Instant.parse("2026-10-17T12:30:00.123Z"))
        store.write("roundtrip.json", document)

        expectThat(store.read<TestDocument>("roundtrip.json")).isEqualTo(document)
    }

    @Test
    fun `when reading document that does not exist should return null`() {
        expectThat(store.read<TestDocument>("does-not-exist.json")).isNull()
    }

    @Test
    fun `should compress large documents and read them back`() {
        val largeDocument = TestDocument("x".repeat(100_000), 1)
        store.write("large.json", largeDocument)

        val storedSize = client.getObjectWithETag(bucketName, "$keyPrefix/large.json")?.bytes?.size
        expectThat(storedSize).isNotNull().isLessThan(10_000)

        expectThat(store.read<TestDocument>("large.json")).isEqualTo(largeDocument)
    }

    @Test
    fun `should only create document if it does not exist`() {
        expectThat(store.create("create.json", TestDocument("first", 1))).isNotNull()
        expectThat(store.create("create.json", TestDocument("second", 2))).isNull()

        expectThat(store.read<TestDocument>("create.json")?.name).isEqualTo("first")
    }

    @Test
    fun `should not replace document with stale ETag`() {
        store.write("replace.json", TestDocument("first", 1))
        val firstVersion = store.readVersioned("replace.json", TestDocument::class.java)!!

        expectThat(store.replace("replace.json", TestDocument("second", 2), firstVersion.etag)).isNotNull()
        expectThat(store.replace("replace.json", TestDocument("stale", 3), firstVersion.etag)).isNull()

        expectThat(store.read<TestDocument>("replace.json")?.name).isEqualTo("second")
    }

    @Test
    fun `should create document on update when it does not exist`() {
        val updated = store.update<CounterDocument>("update-new.json") { current -> CounterDocument((current?.count ?: 0) + 1) }

        expectThat(updated).isEqualTo(CounterDocument(1))
        expectThat(store.read<CounterDocument>("update-new.json")).isEqualTo(CounterDocument(1))
    }

    @Test
    fun `should leave document unchanged when update transform returns null`() {
        store.write("update-noop.json", CounterDocument(5))
        val etagBefore = store.readVersioned("update-noop.json", CounterDocument::class.java)!!.etag

        val result = store.update<CounterDocument>("update-noop.json") { null }

        expectThat(result).isEqualTo(CounterDocument(5))
        expectThat(store.readVersioned("update-noop.json", CounterDocument::class.java)!!.etag).isEqualTo(etagBefore)
    }

    @Test
    fun `should not lose any concurrent updates`() {
        val threadCount = 8
        val updatesPerThread = 10
        val contendedStore = JsonObjectStore(client, bucketName, keyPrefix, maxUpdateAttempts = 200)

        val executor = Executors.newFixedThreadPool(threadCount)
        val futures =
            (1..threadCount).map {
                executor.submit {
                    repeat(updatesPerThread) {
                        contendedStore.update<CounterDocument>("counter.json") { current -> CounterDocument((current?.count ?: 0) + 1) }
                    }
                }
            }
        executor.shutdown()
        expectThat(executor.awaitTermination(2, TimeUnit.MINUTES)).isTrue()
        // Surfaces any exception thrown in a worker thread
        futures.forEach { it.get() }

        expectThat(store.read<CounterDocument>("counter.json")).isEqualTo(CounterDocument(threadCount * updatesPerThread))
    }

    @Test
    fun `should give up after max update attempts when every write conflicts`() {
        val impatientStore = JsonObjectStore(client, bucketName, keyPrefix, maxUpdateAttempts = 3)
        store.write("always-conflicts.json", CounterDocument(0))
        var transformCalls = 0

        expectThrows<ConcurrentUpdateException> {
            impatientStore.update<CounterDocument>("always-conflicts.json") { current ->
                transformCalls++
                // Another writer sneaks in between every read and write
                store.write("always-conflicts.json", CounterDocument(transformCalls * 100))
                CounterDocument(current!!.count + 1)
            }
        }

        expectThat(transformCalls).isEqualTo(3)
    }

    @Test
    fun `should list keys relative to key prefix`() {
        store.write("runs/a/run.json", CounterDocument(1))
        store.write("runs/b/run.json", CounterDocument(2))
        store.write("runs/b/suites/1.json", CounterDocument(3))

        val listing = store.list("runs/", delimiter = "/")
        expectThat(listing.commonPrefixes).containsExactly("runs/a/", "runs/b/")

        expectThat(store.list("runs/b/").objectNames).containsExactly("runs/b/run.json", "runs/b/suites/1.json")
        expectThat(store.list("runs/", startAfter = "runs/a/run.json", limit = 1).objectNames).containsExactly("runs/b/run.json")
    }

    @Test
    fun `should delete document and prefix`() {
        store.write("delete/one.json", CounterDocument(1))
        store.write("delete/nested/two.json", CounterDocument(2))
        store.write("delete-not-me.json", CounterDocument(3))

        store.delete("delete/one.json")
        expectThat(store.read<CounterDocument>("delete/one.json")).isNull()

        store.deletePrefix("delete/")
        expectThat(store.read<CounterDocument>("delete/nested/two.json")).isNull()
        expectThat(store.read<CounterDocument>("delete-not-me.json")).isNotNull()
    }

    @Test
    fun `key prefixes should isolate stores in the same bucket`() {
        val otherStore = JsonObjectStore(client, bucketName, "$keyPrefix-other")
        otherStore.write("isolated.json", CounterDocument(1))

        try {
            expectThat(store.read<CounterDocument>("isolated.json")).isNull()
            expectThat(otherStore.list("").objectNames.size).isGreaterThan(0)
        } finally {
            otherStore.deletePrefix("")
        }
    }
}
