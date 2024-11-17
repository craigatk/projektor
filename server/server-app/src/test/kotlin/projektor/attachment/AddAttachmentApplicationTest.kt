package projektor.attachment

import io.ktor.client.request.*
import io.ktor.client.request.headers
import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.ApplicationTestCaseConfig
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.File

class AddAttachmentApplicationTest : ApplicationTestCase() {
    @Test
    fun `should add attachment to test run then get it`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                attachmentsEnabled = true,
            ),
        ) {
            val publicId = randomPublicId()

            val attachmentBytes = File("src/test/resources/test-attachment.txt").readBytes()

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

            val postResponse =
                client.post("/run/$publicId/attachments/test-attachment.txt") {
                    headers {
                        append("content-length", attachmentBytes.size.toString())
                    }
                    setBody(attachmentBytes)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            waitUntilTestRunHasAttachments(publicId, 1)

            val getResponse = client.get("/run/$publicId/attachments/test-attachment.txt")

            expectThat(getResponse.status).isEqualTo(HttpStatusCode.OK)

            expectThat(getResponse.bodyAsText()).isEqualTo("Here is a test attachment file")
        }

    @Test
    fun `can add attachment to public ID that has no test run yet`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                attachmentsEnabled = true,
            ),
        ) {
            val publicId = randomPublicId()

            val attachmentBytes = File("src/test/resources/test-attachment.txt").readBytes()

            val postResponse =
                client.post("/run/$publicId/attachments/test-attachment.txt") {
                    headers {
                        append("content-length", attachmentBytes.size.toString())
                    }
                    setBody(attachmentBytes)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            waitUntilTestRunHasAttachments(publicId, 1)

            val getResponse = client.get("/run/$publicId/attachments/test-attachment.txt")

            expectThat(getResponse.status).isEqualTo(HttpStatusCode.OK)

            expectThat(getResponse.bodyAsText()).isEqualTo("Here is a test attachment file")
        }

    @Test
    fun `when attachment access key wrong should return error when trying to add attachment`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                attachmentsEnabled = true,
                attachmentsAccessKey = "wrong_access_key",
                attachmentsBucketName = "failtocreate",
                attachmentsAutoCreateBucket = true,
            ),
        ) {
            val publicId = randomPublicId()

            val attachmentBytes = File("src/test/resources/test-attachment.txt").readBytes()

            val postResponse =
                client.post("/run/$publicId/attachments/test-attachment.txt") {
                    headers {
                        append("content-length", attachmentBytes.size.toString())
                    }
                    setBody(attachmentBytes)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.BadRequest)
        }

    @Test
    fun `when attachments not enabled should return 400 when trying to add attachment`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                attachmentsEnabled = false,
            ),
        ) {
            val publicId = randomPublicId()

            val attachmentBytes = File("src/test/resources/test-attachment.txt").readBytes()

            testRunDBGenerator.createSimpleTestRun(publicId)

            val postResponse =
                client.post("/run/$publicId/attachments/test-attachment.txt") {
                    headers {
                        append("content-length", attachmentBytes.size.toString())
                    }
                    setBody(attachmentBytes)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.BadRequest)
        }
}
