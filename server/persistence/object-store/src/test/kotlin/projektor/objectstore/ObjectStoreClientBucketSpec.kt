package projektor.objectstore

import io.kotest.core.spec.style.StringSpec
import projektor.objectstore.bucket.BucketCreationException
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.failed
import strikt.assertions.isA
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

        "when access key is wrong should throw bucket creation exception" {
            val configWithWrongAccessKey = ObjectStoreConfig("http://localhost:9000", "wrong_access_key", "minio_secret_key")
            val bucketName = "wrongaccessbucket"

            expectCatching { ObjectStoreClient(configWithWrongAccessKey).createBucketIfNotExists(bucketName) }
                .failed()
                .isA<BucketCreationException>()
        }
    }
}
