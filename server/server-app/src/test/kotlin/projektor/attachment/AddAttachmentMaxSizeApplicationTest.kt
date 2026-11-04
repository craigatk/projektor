package projektor.attachment

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.ApplicationTestCaseConfig
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
    fun `when attachment without a content length is over max allowed size should return error and not save it`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                attachmentsEnabled = true,
                attachmentsMaxSizeMB = BigDecimal("0.02"),
            ),
        ) {
            val publicId = randomPublicId()

            val response =
                client.post("/run/$publicId/attachments/test-run-summary.png") {
                    setBody(ByteReadChannel(File("src/test/resources/test-run-summary.png").readBytes()))
                }
            expectThat(response.status).isEqualTo(HttpStatusCode.BadRequest)

            val errorResponse = objectMapper.readValue(response.bodyAsText(), AddAttachmentResponse::class.java)
            expectThat(errorResponse.error).isNotNull().and { isEqualTo(AddAttachmentError.ATTACHMENT_TOO_LARGE) }

            expectThat(client.get("/run/$publicId/attachments/test-run-summary.png").status).isEqualTo(HttpStatusCode.NotFound)
        }

    // Over the 10MB size where attachments are uploaded in parts, so a part has already been sent when the limit is hit
    @Test
    fun `when attachment without a content length goes over max allowed size partway through a multipart upload should not save it`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                attachmentsEnabled = true,
                attachmentsMaxSizeMB = BigDecimal("11"),
            ),
        ) {
            val publicId = randomPublicId()

            val response =
                client.post("/run/$publicId/attachments/large.bin") {
                    setBody(ByteReadChannel(ByteArray(12 * 1024 * 1024)))
                }
            expectThat(response.status).isEqualTo(HttpStatusCode.BadRequest)

            val errorResponse = objectMapper.readValue(response.bodyAsText(), AddAttachmentResponse::class.java)
            expectThat(errorResponse.error).isNotNull().and { isEqualTo(AddAttachmentError.ATTACHMENT_TOO_LARGE) }

            expectThat(client.get("/run/$publicId/attachments/large.bin").status).isEqualTo(HttpStatusCode.NotFound)
        }

    @Test
    fun `when attachment without a content length is under max allowed size should save it`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                attachmentsEnabled = true,
                attachmentsMaxSizeMB = BigDecimal("0.04"),
            ),
        ) {
            val publicId = randomPublicId()
            val attachmentBytes = File("src/test/resources/test-run-summary.png").readBytes()

            val response =
                client.post("/run/$publicId/attachments/test-run-summary.png") {
                    setBody(ByteReadChannel(attachmentBytes))
                }
            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            expectThat(client.get("/run/$publicId/attachments/test-run-summary.png").bodyAsBytes().size).isEqualTo(attachmentBytes.size)
        }

    @Test
    fun `when attachment max size configured and attachment size is over max allowed size should return error`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                attachmentsEnabled = true,
                attachmentsMaxSizeMB = BigDecimal("0.02"),
            ),
        ) {
            val publicId = randomPublicId()

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
                client.post("/run/$publicId/attachments/test-run-summary.png") {
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
        projektorTestApplication(
            ApplicationTestCaseConfig(
                attachmentsEnabled = true,
                attachmentsMaxSizeMB = BigDecimal("0.04"),
            ),
        ) {
            val publicId = randomPublicId()

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
                client.post("/run/$publicId/attachments/test-run-summary.png") {
                    headers {
                        append("content-length", "23342")
                    }
                    setBody(File("src/test/resources/test-run-summary.png").readBytes())
                }
            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
        }
}
