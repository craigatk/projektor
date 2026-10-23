package projektor.objectstore.json

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import projektor.objectstore.MissingETagException
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.objectstore.WriteCondition
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JsonObjectStoreConditionalWriteCheckTest {
    private val config = ObjectStoreConfig("http://localhost:9000", "minio_access_key", "minio_secret_key")
    private val client = ObjectStoreClient(config)

    private val bucketName = "jsonbucket"
    private val keyPrefix = "conditional-check-${UUID.randomUUID()}"

    @BeforeAll
    fun createBucket() {
        client.createBucketIfNotExists(bucketName)
    }

    @AfterAll
    fun removeDocuments() {
        JsonObjectStore(client, bucketName, keyPrefix).deletePrefix("")
    }

    @Test
    fun `should pass the conditional write check on a store that honors write conditions`() {
        val store = JsonObjectStore(client, bucketName, keyPrefix)

        store.verifyConditionalWrites()

        // The check cleans up after itself
        expectThat(store.list("").objectNames).isEqualTo(listOf())
    }

    @Test
    fun `should fail the conditional write check on a store that ignores write conditions`() {
        val store = JsonObjectStore(IgnoresWriteConditionsClient(config), bucketName, keyPrefix)

        expectThrows<ConditionalWritesNotSupportedException> { store.verifyConditionalWrites() }
            .get { message }.isNotNull().contains("If-None-Match")
    }

    // A missing ETag (e.g. stripped by a proxy) would otherwise make every If-Match update fail as a conflict
    @Test
    fun `should normalize a quoted ETag and reject a missing or blank one`() {
        expectThat(ObjectStoreClient.requireETag("\"abc123\"", bucketName, "key")).isEqualTo("abc123")

        expectThrows<MissingETagException> { ObjectStoreClient.requireETag(null, bucketName, "key") }
        expectThrows<MissingETagException> { ObjectStoreClient.requireETag("", bucketName, "key") }
        expectThrows<MissingETagException> { ObjectStoreClient.requireETag("\"\"", bucketName, "key") }
    }

    /**
     * Like an S3-compatible store that accepts conditional headers but doesn't act on them.
     */
    private class IgnoresWriteConditionsClient(config: ObjectStoreConfig) : ObjectStoreClient(config) {
        override fun putObject(
            bucketName: String,
            objectName: String,
            bytes: ByteArray,
            contentType: String,
            condition: WriteCondition,
        ): String = super.putObject(bucketName, objectName, bytes, contentType, WriteCondition.None)
    }
}
