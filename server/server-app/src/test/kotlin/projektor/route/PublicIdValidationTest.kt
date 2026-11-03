package projektor.route

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.concurrent.atomic.AtomicInteger

class PublicIdValidationTest {
    private val handlerCalls = AtomicInteger()

    private fun ApplicationTestBuilder.validatedRoutes() {
        routing {
            install(PublicIdValidation)

            get("/run/{publicId}") {
                handlerCalls.incrementAndGet()
                call.respondText(call.parameters["publicId"]!!)
            }
            post("/run/{publicId}/attachments/{attachmentName}") {
                handlerCalls.incrementAndGet()
                call.respondText("saved")
            }
            get("/health") {
                call.respondText("OK")
            }
        }
    }

    @Test
    fun `should pass through alphanumeric public IDs`() =
        testApplication {
            validatedRoutes()

            val response = client.get("/run/ABC123xyz")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            expectThat(response.bodyAsText()).isEqualTo("ABC123xyz")
        }

    @Test
    fun `should not let an encoded slash in a public ID address other object store keys`() =
        testApplication {
            validatedRoutes()

            val response = client.post("/run/created%2F2020/attachments/01-01%2Fx.json")

            expectThat(response.status).isEqualTo(HttpStatusCode.NotFound)
            expectThat(handlerCalls.get()).isEqualTo(0)
        }

    @Test
    fun `should reject public IDs with path traversal or other punctuation`() =
        testApplication {
            validatedRoutes()

            listOf("..%2Fx", "a-b", "a.b", "a%20b").forEach { publicId ->
                expectThat(client.get("/run/$publicId").status).isEqualTo(HttpStatusCode.NotFound)
            }
            expectThat(handlerCalls.get()).isEqualTo(0)
        }

    @Test
    fun `should not affect routes without a public ID`() =
        testApplication {
            validatedRoutes()

            expectThat(client.get("/health").status).isEqualTo(HttpStatusCode.OK)
        }
}
