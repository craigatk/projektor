package projektor.attachment

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.attachments.AddAttachmentError
import projektor.server.api.attachments.AddAttachmentResponse
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.io.File
import java.math.BigDecimal

class AddAttachmentMaxSizeApplicationTest : ApplicationTestCase() {
    @Test
    fun `when attachment max size configured and attachment size is over max allowed size should return error`() =
        testSuspend {
            val publicId = randomPublicId()
            attachmentsEnabled = true
            attachmentsMaxSizeMB = BigDecimal("0.02")

            startTestServer()

            testRunDBGenerator.createTestRun(
                publicId,
                listOf(
                    TestSuiteData(
                        "testSuite1",
                        listOf("testSuite1TestCase1", "testSuite1TestCase2"),
                        listOf(),
                        listOf(),
                    ),
                ),
            )

            val response =
                testClient.post("/run/$publicId/attachments/test-run-summary.png") {
                    headers {
                        append("content-length", "23342")
                    }
                    setBody(File("src/test/resources/test-run-summary.png").readBytes())
                }
            expectThat(response.status).isEqualTo(HttpStatusCode.BadRequest)

            val errorResponse = objectMapper.readValue(response.bodyAsText(), AddAttachmentResponse::class.java)
            expectThat(errorResponse.error).isNotNull().and { isEqualTo(AddAttachmentError.ATTACHMENT_TOO_LARGE) }
        }

    @Test
    fun `when attachment max size configured and attachment size is under max allowed size should succeed`() =
        testSuspend {
            val publicId = randomPublicId()
            attachmentsEnabled = true
            attachmentsMaxSizeMB = BigDecimal("0.04")

            startTestServer()

            testRunDBGenerator.createTestRun(
                publicId,
                listOf(
                    TestSuiteData(
                        "testSuite1",
                        listOf("testSuite1TestCase1", "testSuite1TestCase2"),
                        listOf(),
                        listOf(),
                    ),
                ),
            )

            val response =
                testClient.post("/run/$publicId/attachments/test-run-summary.png") {
                    headers {
                        append("content-length", "23342")
                    }
                    setBody(File("src/test/resources/test-run-summary.png").readBytes())
                }
            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
        }
}
