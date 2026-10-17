package projektor.objectstore

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Listing and deleting more objects than fit in one S3 request (1000 keys), which the client pages through
 * when listing and splits into batches when deleting.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ObjectStoreClientManyObjectsTest {
    private val config = ObjectStoreConfig("http://localhost:9000", "minio_access_key", "minio_secret_key")
    private val client = ObjectStoreClient(config)

    private val bucketName = "manyobjectsbucket"

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
    fun `should list and delete more objects than fit in a single request`() {
        val listPrefix = "${prefix}many/"
        val objectNames = (1..OBJECT_COUNT).map { "${listPrefix}${it.toString().padStart(5, '0')}" }
        putObjects(objectNames)

        val allObjects = client.listObjects(bucketName, listPrefix)
        expectThat(allObjects.objectNames).isEqualTo(objectNames)

        val limitedObjects = client.listObjects(bucketName, listPrefix, limit = 1100)
        expectThat(limitedObjects.objectNames).isEqualTo(objectNames.take(1100))

        val objectsAfterFirstPage = client.listObjects(bucketName, listPrefix, startAfter = objectNames[999])
        expectThat(objectsAfterFirstPage.objectNames).isEqualTo(objectNames.drop(1000))

        val keepName = "${prefix}keep"
        putObjects(listOf(keepName))

        client.removeObjectsWithPrefix(bucketName, listPrefix)

        expectThat(client.listObjects(bucketName, listPrefix).objectNames).isEmpty()
        expectThat(client.getObjectWithETag(bucketName, keepName)).isNotNull()
    }

    @Test
    fun `should list more common prefixes than fit in a single request`() {
        val listPrefix = "${prefix}prefixes/"
        val objectNames = (1..OBJECT_COUNT).map { "${listPrefix}${it.toString().padStart(5, '0')}/object" }
        putObjects(objectNames)

        val listing = client.listObjects(bucketName, listPrefix, delimiter = "/")

        expectThat(listing.commonPrefixes).hasSize(OBJECT_COUNT)
        expectThat(listing.objectNames).isEmpty()
    }

    private fun putObjects(objectNames: List<String>) {
        val executor = Executors.newFixedThreadPool(16)
        val futures =
            objectNames.map {
                    objectName ->
                executor.submit { client.putObject(bucketName, objectName, byteArrayOf(), "text/plain") }
            }
        futures.forEach { it.get() }
        executor.shutdown()
        executor.awaitTermination(1, TimeUnit.MINUTES)
    }

    companion object {
        // More than two S3 list pages and delete batches, so the final partial page/batch is covered too
        private const val OBJECT_COUNT = 2050
    }
}
