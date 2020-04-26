package projektor.version

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@KtorExperimentalAPI
class VersionApplicationTest : ApplicationTestCase() {
    @Test
    fun `should provide version endpoint`() {
        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/version").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                expectThat(response.content).isNotNull().and { contains("version") }
            }
        }
    }
}
