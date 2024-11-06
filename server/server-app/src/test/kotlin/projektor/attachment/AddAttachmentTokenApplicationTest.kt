package projektor.attachment

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.auth.AuthConfig
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.File

class AddAttachmentTokenApplicationTest : ApplicationTestCase() {
    @Test
    fun `when token required and valid token included in header should add attachment`() =
        testSuspend {
            val validPublishToken = "publish12345"
            publishToken = validPublishToken

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
                        append(AuthConfig.PUBLISH_TOKEN, validPublishToken)
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
    fun `when token required and invalid token included in header should not add attachment`() =
        testSuspend {
            val validPublishToken = "publish12345"
            publishToken = validPublishToken

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
                        append(AuthConfig.PUBLISH_TOKEN, "invalidPublishToken")
                    }
                    setBody(attachmentBytes)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.Unauthorized)

            val getResponse = testClient.get("/run/$publicId/attachments/test-attachment.txt")
            expectThat(getResponse.status).isEqualTo(HttpStatusCode.NotFound)
        }
}
