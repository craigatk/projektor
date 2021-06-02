package projektor.config

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
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
    fun `should return cleanup age in days when it is configured`() {
        reportCleanupMaxAgeDays = 60

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/config").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val serverConfig = objectMapper.readValue(response.content, ServerConfig::class.java)
                expectThat(serverConfig.cleanup.enabled).isTrue()
                expectThat(serverConfig.cleanup.maxReportAgeInDays).isEqualTo(60)
            }
        }
    }

    @Test
    fun `should return cleanup disabled when it is not configured`() {
        reportCleanupMaxAgeDays = null

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/config").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val serverConfig = objectMapper.readValue(response.content, ServerConfig::class.java)
                expectThat(serverConfig.cleanup.enabled).isFalse()
                expectThat(serverConfig.cleanup.maxReportAgeInDays).isNull()
            }
        }
    }
}
