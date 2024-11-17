package projektor.attachment

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.ApplicationTestCaseConfig
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.attachments.Attachments
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.io.File
import kotlin.test.assertNotNull

class ListAttachmentsApplicationTest : ApplicationTestCase() {
    @Test
    fun `should add attachments to test run then list them`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                attachmentsEnabled = true,
            ),
        ) {
            val publicId = randomPublicId()

            val attachment1FileName = "test-attachment.txt"
            val attachment1Bytes = File("src/test/resources/$attachment1FileName").readBytes()

            val attachment2FileName = "test-run-summary.png"
            val attachment2Bytes = File("src/test/resources/$attachment2FileName").readBytes()

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

            val postResponse1 =
                client.post("/run/$publicId/attachments/$attachment1FileName") {
                    headers {
                        append("content-length", attachment1Bytes.size.toString())
                    }
                    setBody(attachment1Bytes)
                }
            expectThat(postResponse1.status).isEqualTo(HttpStatusCode.OK)

            val postResponse2 =
                client.post("/run/$publicId/attachments/$attachment2FileName") {
                    headers {
                        append("content-length", attachment2Bytes.size.toString())
                    }
                    setBody(attachment2Bytes)
                }
            expectThat(postResponse2.status).isEqualTo(HttpStatusCode.OK)

            waitUntilTestRunHasAttachments(publicId, 2)

            val getResponse = client.get("/run/$publicId/attachments")
            expectThat(getResponse.status).isEqualTo(HttpStatusCode.OK)

            val attachmentsResponse = objectMapper.readValue(getResponse.bodyAsText(), Attachments::class.java)
            assertNotNull(attachmentsResponse)

            expectThat(attachmentsResponse.attachments).hasSize(2).and {
                any {
                    get { fileName }.isEqualTo(attachment1FileName)
                    get { fileSize }.isNotNull().and { isEqualTo(attachment1Bytes.size.toLong()) }
                }
                any {
                    get { fileName }.isEqualTo(attachment2FileName)
                    get { fileSize }.isNotNull().and { isEqualTo(attachment2Bytes.size.toLong()) }
                }
            }
        }
}
