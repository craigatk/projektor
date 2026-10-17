package projektor.objectstore

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ObjectStoreClientConditionalTest {
    private val config = ObjectStoreConfig("http://localhost:9000", "minio_access_key", "minio_secret_key")
    private val client = ObjectStoreClient(config)

    private val bucketName = "conditionalbucket"

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

    @Test
    fun `should return ETag when reading object`() {
        val objectName = "${prefix}etag"
        val writeETag = client.putObject(bucketName, objectName, "contents".toByteArray(), "text/plain")

        val storedObject = client.getObjectWithETag(bucketName, objectName)

        expectThat(storedObject).isNotNull().and {
            get { bytes.decodeToString() }.isEqualTo("contents")
            get { etag }.isEqualTo(writeETag)
        }
    }

    @Test
    fun `when reading object with ETag that does not exist should return null`() {
        expectThat(client.getObjectWithETag(bucketName, "${prefix}does-not-exist")).isNull()
    }

    @Test
    fun `should only create object if it does not exist`() {
        val objectName = "${prefix}create-once"
        client.putObject(bucketName, objectName, "first".toByteArray(), "text/plain", WriteCondition.IfNotExists)

        expectThrows<PreconditionFailedException> {
            client.putObject(bucketName, objectName, "second".toByteArray(), "text/plain", WriteCondition.IfNotExists)
        }

        expectThat(client.getObjectWithETag(bucketName, objectName)?.bytes?.decodeToString()).isEqualTo("first")
    }

    @Test
    fun `should only replace object if ETag matches`() {
        val objectName = "${prefix}if-match"
        val firstETag = client.putObject(bucketName, objectName, "first".toByteArray(), "text/plain")
        val secondETag =
            client.putObject(bucketName, objectName, "second".toByteArray(), "text/plain", WriteCondition.IfMatch(firstETag))
        expectThat(secondETag).isNotEqualTo(firstETag)

        expectThrows<PreconditionFailedException> {
            client.putObject(bucketName, objectName, "stale".toByteArray(), "text/plain", WriteCondition.IfMatch(firstETag))
        }

        expectThat(client.getObjectWithETag(bucketName, objectName)?.bytes?.decodeToString()).isEqualTo("second")
    }

    @Test
    fun `should list objects under prefix in order`() {
        val listPrefix = "${prefix}list/"
        listOf("c", "a", "b").forEach { client.putObject(bucketName, "$listPrefix$it", byteArrayOf(), "text/plain") }

        val listing = client.listObjects(bucketName, listPrefix)

        expectThat(listing.objectNames).containsExactly("${listPrefix}a", "${listPrefix}b", "${listPrefix}c")
        expectThat(listing.commonPrefixes).isEmpty()
    }

    @Test
    fun `should list with start-after and limit`() {
        val listPrefix = "${prefix}paged/"
        (1..5).forEach { client.putObject(bucketName, "${listPrefix}$it", byteArrayOf(), "text/plain") }

        val listing = client.listObjects(bucketName, listPrefix, startAfter = "${listPrefix}2", limit = 2)

        expectThat(listing.objectNames).containsExactly("${listPrefix}3", "${listPrefix}4")
    }

    @Test
    fun `should roll up nested keys into common prefixes when listing with delimiter`() {
        val listPrefix = "${prefix}delimited/"
        client.putObject(bucketName, "${listPrefix}top", byteArrayOf(), "text/plain")
        client.putObject(bucketName, "${listPrefix}repo1/run1", byteArrayOf(), "text/plain")
        client.putObject(bucketName, "${listPrefix}repo1/run2", byteArrayOf(), "text/plain")
        client.putObject(bucketName, "${listPrefix}repo2/run1", byteArrayOf(), "text/plain")

        val listing = client.listObjects(bucketName, listPrefix, delimiter = "/")

        expectThat(listing.objectNames).containsExactly("${listPrefix}top")
        expectThat(listing.commonPrefixes).containsExactly("${listPrefix}repo1/", "${listPrefix}repo2/")
    }

    @Test
    fun `should remove all objects with prefix`() {
        val removePrefix = "${prefix}remove/"
        (1..3).forEach { client.putObject(bucketName, "${removePrefix}nested/$it", byteArrayOf(), "text/plain") }
        client.putObject(bucketName, "${prefix}keep", byteArrayOf(), "text/plain")

        client.removeObjectsWithPrefix(bucketName, removePrefix)

        expectThat(client.listObjects(bucketName, removePrefix).objectNames).isEmpty()
        expectThat(client.getObjectWithETag(bucketName, "${prefix}keep")).isNotNull()
    }
}
