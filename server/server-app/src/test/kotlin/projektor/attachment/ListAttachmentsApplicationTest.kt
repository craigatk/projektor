package projektor.attachment

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotNull
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.attachments.Attachments
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@KtorExperimentalAPI
class ListAttachmentsApplicationTest : ApplicationTestCase() {
    @Test
    fun `should add attachments to test run then list them`() {
        val publicId = randomPublicId()
        attachmentsEnabled = true

        val attachment1FileName = "test-attachment.txt"
        val attachment1Bytes = File("src/test/resources/$attachment1FileName").readBytes()

        val attachment2FileName = "test-run-summary.png"
        val attachment2Bytes = File("src/test/resources/$attachment2FileName").readBytes()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/attachments/$attachment1FileName") {
                testRunDBGenerator.createTestRun(
                        publicId,
                        listOf(
                                TestSuiteData("testSuite1",
                                        listOf("testSuite1TestCase1", "testSuite1TestCase2"),
                                        listOf(),
                                        listOf()
                                )
                        )
                )
                addHeader("content-length", attachment1Bytes.size.toString())
                setBody(attachment1Bytes)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
            }
            handleRequest(HttpMethod.Post, "/run/$publicId/attachments/$attachment2FileName") {
                addHeader("content-length", attachment2Bytes.size.toString())
                setBody(attachment2Bytes)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
            }

            waitUntilTestRunHasAttachments(publicId, 2)

            handleRequest(HttpMethod.Get, "/run/$publicId/attachments") {
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val attachmentsResponse = objectMapper.readValue(response.content, Attachments::class.java)
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
    }
}
