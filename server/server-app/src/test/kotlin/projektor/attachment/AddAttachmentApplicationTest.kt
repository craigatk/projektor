package projektor.attachment

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.File

@ExperimentalStdlibApi
class AddAttachmentApplicationTest : ApplicationTestCase() {
    @Test
    fun `should add attachment to test run then get it`() {
        val publicId = randomPublicId()
        attachmentsEnabled = true

        val attachmentBytes = File("src/test/resources/test-attachment.txt").readBytes()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/attachments/test-attachment.txt") {
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

                addHeader("content-length", attachmentBytes.size.toString())
                setBody(attachmentBytes)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
            }

            waitUntilTestRunHasAttachments(publicId, 1)

            handleRequest(HttpMethod.Get, "/run/$publicId/attachments/test-attachment.txt") {
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                expectThat(response.byteContent?.decodeToString()).isEqualTo("Here is a test attachment file")
            }
        }
    }

    @Test
    fun `can add attachment to public ID that has no test run yet`() {
        val publicId = randomPublicId()
        attachmentsEnabled = true

        val attachmentBytes = File("src/test/resources/test-attachment.txt").readBytes()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/attachments/test-attachment.txt") {
                addHeader("content-length", attachmentBytes.size.toString())
                setBody(attachmentBytes)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
            }

            waitUntilTestRunHasAttachments(publicId, 1)

            handleRequest(HttpMethod.Get, "/run/$publicId/attachments/test-attachment.txt") {
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                expectThat(response.byteContent?.decodeToString()).isEqualTo("Here is a test attachment file")
            }
        }
    }

    @Test
    fun `when attachment access key wrong should return error when trying to add attachment`() {
        val publicId = randomPublicId()
        attachmentsEnabled = true
        attachmentsAccessKey = "wrong_access_key"
        attachmentsBucketName = "failtocreate"
        attachmentsAutoCreateBucket = true

        val attachmentBytes = File("src/test/resources/test-attachment.txt").readBytes()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/attachments/test-attachment.txt") {
                addHeader("content-length", attachmentBytes.size.toString())
                setBody(attachmentBytes)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
            }
        }
    }

    @Test
    fun `when attachments not enabled should return 400 when trying to add attachment`() {
        val publicId = randomPublicId()
        attachmentsEnabled = false

        val attachmentBytes = File("src/test/resources/test-attachment.txt").readBytes()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/attachments/test-attachment.txt") {
                testRunDBGenerator.createSimpleTestRun(publicId)

                addHeader("content-length", attachmentBytes.size.toString())
                setBody(attachmentBytes)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
            }
        }
    }
}
