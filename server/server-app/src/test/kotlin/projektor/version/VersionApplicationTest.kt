package projektor.version

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

class VersionApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should provide version endpoint`() =
        testSuspend {
            val response = testClient.get("/version")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            expectThat(response.bodyAsText()).contains("version")
        }
}
