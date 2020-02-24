package projektor.objectstore

import io.kotlintest.specs.StringSpec
import io.minio.errors.ErrorResponseException
import strikt.api.expectThat
import strikt.assertions.containsIgnoringCase
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

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

            expectThat(client.getObject(bucketName, objectName))
                    .isNotNull()
                    .and { get { readBytes().decodeToString() }.isEqualTo("Here is a test file") }
        }

        "when trying to get object that does not exist should return null"() {
            expectThat(client.getObject(bucketName, "objectDoesNotExist"))
                    .isNull()
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
