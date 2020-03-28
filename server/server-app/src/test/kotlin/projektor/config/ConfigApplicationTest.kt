package projektor.config

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.Test
import kotlinx.coroutines.ObsoleteCoroutinesApi
import projektor.ApplicationTestCase
import projektor.server.api.config.ServerConfig
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
class ConfigApplicationTest : ApplicationTestCase() {
    @Test
    fun `should return cleanup age in days when it is configured`() {
        cleanupMaxAgeDays = 60

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/config").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val serverConfig = objectMapper.readValue(response.content, ServerConfig::class.java)
                expectThat(serverConfig.cleanupMaxReportAgeDays).isEqualTo(60)
            }
        }
    }

    @Test
    fun `should return cleanup age of 0 days when it is not configured`() {
        cleanupMaxAgeDays = null

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/config").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val serverConfig = objectMapper.readValue(response.content, ServerConfig::class.java)
                expectThat(serverConfig.cleanupMaxReportAgeDays).isEqualTo(0)
            }
        }
    }
}
