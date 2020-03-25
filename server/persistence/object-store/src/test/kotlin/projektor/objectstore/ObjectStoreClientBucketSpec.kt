package projektor.objectstore

import io.kotlintest.specs.StringSpec
import strikt.api.expectThat
import strikt.assertions.isTrue

class ObjectStoreClientBucketSpec : StringSpec() {
    private val config = ObjectStoreConfig("http://localhost:9000", "minio_access_key", "minio_secret_key")
    private val client = ObjectStoreClient(config)

    init {
        "should create bucket" {
            val bucketName = "testbucket"
            client.createBucketIfNotExists(bucketName)

            expectThat(client.bucketExists(bucketName)).isTrue()
        }
    }
}
