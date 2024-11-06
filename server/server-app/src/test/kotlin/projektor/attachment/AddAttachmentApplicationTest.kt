package projektor.attachment

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.File

class AddAttachmentApplicationTest : ApplicationTestCase() {
    @Test
    fun `should add attachment to test run then get it`() =
        testSuspend {
            val publicId = randomPublicId()
            attachmentsEnabled = true

            startTestServer()

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
                testClient.post("/run/$publicId/attachments/test-attachment.txt") {
                    headers {
                        append("content-length", attachmentBytes.size.toString())
                    }
                    setBody(attachmentBytes)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            waitUntilTestRunHasAttachments(publicId, 1)

            val getResponse = testClient.get("/run/$publicId/attachments/test-attachment.txt")

            expectThat(getResponse.status).isEqualTo(HttpStatusCode.OK)

            expectThat(getResponse.bodyAsText()).isEqualTo("Here is a test attachment file")
        }

    @Test
    fun `can add attachment to public ID that has no test run yet`() =
        testSuspend {
            val publicId = randomPublicId()
            attachmentsEnabled = true

            startTestServer()

            val attachmentBytes = File("src/test/resources/test-attachment.txt").readBytes()

            val postResponse =
                testClient.post("/run/$publicId/attachments/test-attachment.txt") {
                    headers {
                        append("content-length", attachmentBytes.size.toString())
                    }
                    setBody(attachmentBytes)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            waitUntilTestRunHasAttachments(publicId, 1)

            val getResponse = testClient.get("/run/$publicId/attachments/test-attachment.txt")

            expectThat(getResponse.status).isEqualTo(HttpStatusCode.OK)

            expectThat(getResponse.bodyAsText()).isEqualTo("Here is a test attachment file")
        }

    @Test
    fun `when attachment access key wrong should return error when trying to add attachment`() =
        testSuspend {
            val publicId = randomPublicId()
            attachmentsEnabled = true
            attachmentsAccessKey = "wrong_access_key"
            attachmentsBucketName = "failtocreate"
            attachmentsAutoCreateBucket = true

            val attachmentBytes = File("src/test/resources/test-attachment.txt").readBytes()

            startTestServer()

            val postResponse =
                testClient.post("/run/$publicId/attachments/test-attachment.txt") {
                    headers {
                        append("content-length", attachmentBytes.size.toString())
                    }
                    setBody(attachmentBytes)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.BadRequest)
        }

    @Test
    fun `when attachments not enabled should return 400 when trying to add attachment`() =
        testSuspend {
            val publicId = randomPublicId()
            attachmentsEnabled = false

            val attachmentBytes = File("src/test/resources/test-attachment.txt").readBytes()

            startTestServer()

            testRunDBGenerator.createSimpleTestRun(publicId)

            val postResponse =
                testClient.post("/run/$publicId/attachments/test-attachment.txt") {
                    headers {
                        append("content-length", attachmentBytes.size.toString())
                    }
                    setBody(attachmentBytes)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.BadRequest)
        }
}
