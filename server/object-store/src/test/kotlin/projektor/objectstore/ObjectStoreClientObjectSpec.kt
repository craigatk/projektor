package projektor.objectstore

import io.kotlintest.specs.StringSpec
import io.minio.errors.ErrorResponseException
import strikt.api.expectThat
import strikt.assertions.containsIgnoringCase
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@ExperimentalStdlibApi
class ObjectStoreClientObjectSpec : StringSpec() {
    private val config = ObjectStoreConfig("http://localhost:9000", "minio_access_key", "minio_secret_key")
    private val client = ObjectStoreClient(config)

    private val bucketName = "objectbucket"
    private val objectName = "thetestobject"

    override fun listeners() = listOf(ObjectCleanupListener(client, bucketName, objectName))

    init {
        "should store and retrieve object" {
            client.putObject(bucketName, objectName, "src/test/resources/test_file.txt")

            expectThat(client.getObject(bucketName, objectName).readBytes().decodeToString()).isEqualTo("Here is a test file")
        }

        "should delete object"() {
            client.putObject(bucketName, objectName, "src/test/resources/test_file.txt")

            client.removeObject(bucketName, objectName)

            try {
                client.getObject(bucketName, objectName)
            } catch (e: ErrorResponseException) {
                expectThat(e.message).isNotNull().and {
                    containsIgnoringCase("does not exist")
                }
            }
        }
    }
}
