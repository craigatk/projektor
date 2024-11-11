package projektor.message

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
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
    fun `when single global message should return it`() =
        testSuspend {
            val publicId = randomPublicId()

            globalMessages = "Here is a global message"

            startTestServer()

            val response = testClient.get("/run/$publicId/messages")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val messagesResponse = objectMapper.readValue(response.bodyAsText(), Messages::class.java)
            assertNotNull(messagesResponse)

            expectThat(messagesResponse.messages)
                .hasSize(1)
                .contains("Here is a global message")
        }

    @Test
    fun `when two global messages should return them`() =
        testSuspend {
            val publicId = randomPublicId()

            globalMessages = "First global message|Second global message"

            startTestServer()

            val response = testClient.get("/run/$publicId/messages")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val messagesResponse = objectMapper.readValue(response.bodyAsText(), Messages::class.java)
            assertNotNull(messagesResponse)

            expectThat(messagesResponse.messages)
                .hasSize(2)
                .contains("First global message")
                .contains("Second global message")
        }

    @Test
    fun `when no messages should return empty list`() =
        testSuspend {
            val publicId = randomPublicId()

            globalMessages = null

            startTestServer()

            val response = testClient.get("/run/$publicId/messages")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val messagesResponse = objectMapper.readValue(response.bodyAsText(), Messages::class.java)
            assertNotNull(messagesResponse)

            expectThat(messagesResponse.messages)
                .hasSize(0)
        }
}
