package projektor.health

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import projektor.ApplicationTestCase
import projektor.route.Status
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class HealthApplicationTest : ApplicationTestCase() {
    @Test
    fun `should return healthy status`() {
        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/health").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val healthStatus = objectMapper.readValue(response.content, Status::class.java)
                expectThat(healthStatus.status).isEqualTo("OK")
            }
        }
    }
}
