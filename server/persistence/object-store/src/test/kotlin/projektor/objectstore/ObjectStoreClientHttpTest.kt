package projektor.objectstore

import io.minio.Http
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isLessThan
import strikt.assertions.isTrue
import java.util.UUID

class ObjectStoreClientHttpTest {
    @Test
    fun `should allow as many concurrent requests to the store as the IO dispatcher has threads`() {
        val httpClient = ObjectStoreClient.createHttpClient()

        expectThat(httpClient.dispatcher.maxRequestsPerHost).isEqualTo(ObjectStoreClient.MAX_CONCURRENT_REQUESTS)
        expectThat(httpClient.dispatcher.maxRequests).isEqualTo(ObjectStoreClient.MAX_CONCURRENT_REQUESTS)
    }

    @Test
    fun `should fail stalled requests sooner than the default client, and still retry failed ones`() {
        val httpClient = ObjectStoreClient.createHttpClient()
        val defaultClient = Http.newDefaultClient()

        expectThat(httpClient.readTimeoutMillis).isLessThan(defaultClient.readTimeoutMillis)
        expectThat(httpClient.writeTimeoutMillis).isLessThan(defaultClient.writeTimeoutMillis)
        expectThat(httpClient.connectTimeoutMillis).isLessThan(defaultClient.connectTimeoutMillis)
        expectThat(httpClient.interceptors.any { it is Http.StatusRetryInterceptor }).isTrue()
    }

    @Test
    fun `should tell whether an object exists`() {
        val bucketName = "objectexists"
        val objectName = "exists-${UUID.randomUUID()}"
        val client = ObjectStoreClient(ObjectStoreConfig("http://localhost:9000", "minio_access_key", "minio_secret_key"))
        client.createBucketIfNotExists(bucketName)

        expectThat(client.objectExists(bucketName, objectName)).isFalse()

        client.putObject(bucketName, objectName, "contents".toByteArray(), "text/plain")
        expectThat(client.objectExists(bucketName, objectName)).isTrue()

        client.removeObject(bucketName, objectName)
        expectThat(client.objectExists(bucketName, objectName)).isFalse()
    }
}
