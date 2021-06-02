package projektor.message

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.messages.Messages
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

class MessageApplicationTest : ApplicationTestCase() {

    @Test
    fun `when single global message should return it`() {
        val publicId = randomPublicId()

        globalMessages = "Here is a global message"

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/messages")
                .apply {
                    expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                    val messagesResponse = objectMapper.readValue(response.content, Messages::class.java)
                    assertNotNull(messagesResponse)

                    expectThat(messagesResponse.messages)
                        .hasSize(1)
                        .contains("Here is a global message")
                }
        }
    }

    @Test
    fun `when two global messages should return them`() {
        val publicId = randomPublicId()

        globalMessages = "First global message|Second global message"

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/messages")
                .apply {
                    expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                    val messagesResponse = objectMapper.readValue(response.content, Messages::class.java)
                    assertNotNull(messagesResponse)

                    expectThat(messagesResponse.messages)
                        .hasSize(2)
                        .contains("First global message")
                        .contains("Second global message")
                }
        }
    }

    @Test
    fun `when no messages should return empty list`() {
        val publicId = randomPublicId()

        globalMessages = null

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/messages")
                .apply {
                    expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                    val messagesResponse = objectMapper.readValue(response.content, Messages::class.java)
                    assertNotNull(messagesResponse)

                    expectThat(messagesResponse.messages)
                        .hasSize(0)
                }
        }
    }
}
