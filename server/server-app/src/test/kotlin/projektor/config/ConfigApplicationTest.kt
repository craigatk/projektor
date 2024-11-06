package projektor.config

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.server.api.config.ServerConfig
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

class ConfigApplicationTest : ApplicationTestCase() {
    @Test
    fun `should return cleanup age in days when it is configured`() =
        testSuspend {
            reportCleanupMaxAgeDays = 60

            startTestServer()

            val response = testClient.get("/config")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val serverConfig = objectMapper.readValue(response.bodyAsText(), ServerConfig::class.java)
            expectThat(serverConfig.cleanup.enabled).isTrue()
            expectThat(serverConfig.cleanup.maxReportAgeInDays).isEqualTo(60)
        }

    @Test
    fun `should return cleanup disabled when it is not configured`() =
        testSuspend {
            reportCleanupMaxAgeDays = null

            startTestServer()

            val response = testClient.get("/config")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val serverConfig = objectMapper.readValue(response.bodyAsText(), ServerConfig::class.java)
            expectThat(serverConfig.cleanup.enabled).isFalse()
            expectThat(serverConfig.cleanup.maxReportAgeInDays).isNull()
        }
}
