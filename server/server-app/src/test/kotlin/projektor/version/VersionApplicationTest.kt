package projektor.version

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

class VersionApplicationTest : ApplicationTestCase() {
    @Test
    fun `should provide version endpoint`() =
        projektorTestApplication {
            val response = client.get("/version")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            expectThat(response.bodyAsText()).contains("version")
        }
}
