package projektor.attachment

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.ApplicationTestCaseConfig
import projektor.TestSuiteData
import projektor.auth.AuthConfig
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.File

class AddAttachmentTokenApplicationTest : ApplicationTestCase() {
    @Test
    fun `when token required and valid token included in header should add attachment`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                publishToken = "publish12345",
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
                        append(AuthConfig.PUBLISH_TOKEN, "publish12345")
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
    fun `when token required and invalid token included in header should not add attachment`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                publishToken = "publish12345",
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
                        append(AuthConfig.PUBLISH_TOKEN, "invalidPublishToken")
                    }
                    setBody(attachmentBytes)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.Unauthorized)

            val getResponse = client.get("/run/$publicId/attachments/test-attachment.txt")
            expectThat(getResponse.status).isEqualTo(HttpStatusCode.NotFound)
        }
}
