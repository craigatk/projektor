package projektor.health

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.route.Status
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

class HealthApplicationTest : ApplicationTestCase() {
    @Test
    fun `should return healthy status and not log call to health endpoint`() =
        projektorTestApplication {
            val response = client.get("/health")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val healthStatus = objectMapper.readValue(response.bodyAsText(), Status::class.java)
            expectThat(healthStatus.status).isEqualTo("OK")

            val logContents = getLogContents()

            expectThat(logContents).not { contains(("/health")) }
        }
}
