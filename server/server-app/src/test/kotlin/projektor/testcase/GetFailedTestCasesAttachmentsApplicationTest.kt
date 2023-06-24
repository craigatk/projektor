package projektor.testcase

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestCase
import projektor.server.api.attachments.AttachmentType
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import kotlin.test.assertNotNull

class GetFailedTestCasesAttachmentsApplicationTest : ApplicationTestCase() {

    @Test
    fun `should include screenshot attachment when test case has one`() {
        val publicId = randomPublicId()

        attachmentsEnabled = true

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/${publicId.id}/cases/failed") {
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(
                        TestSuiteData(
                            "testSuite1",
                            listOf("testSuite1PassedTestCase1", "testSuite1PassedTestCase2"),
                            listOf("testSuite1 FailedTestCase 1", "testSuite1 FailedTestCase 2"),
                            listOf()
                        ),
                    )
                )

                testRunDBGenerator.addAttachment(publicId, "object-1", "testSuite1 FailedTestCase 1.png")
                testRunDBGenerator.addAttachment(publicId, "object-2", "testSuite1 FailedTestCase 2.png")
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val responseContent = response.content
                assertNotNull(responseContent)
                val failedTestCases: List<TestCase> = objectMapper.readValue(responseContent)

                expectThat(failedTestCases)
                    .hasSize(2)

                val failedTestCase1 = failedTestCases.find { it.name == "testSuite1 FailedTestCase 1" }
                expectThat(failedTestCase1).isNotNull().and {
                    get { attachments }.isNotNull()
                        .hasSize(1)
                        .any {
                            get { fileName }.isEqualTo("testSuite1 FailedTestCase 1.png")
                            get { attachmentType }.isEqualTo(AttachmentType.IMAGE)
                        }
                }

                val failedTestCase2 = failedTestCases.find { it.name == "testSuite1 FailedTestCase 2" }
                expectThat(failedTestCase2).isNotNull().and {
                    get { attachments }
                        .isNotNull()
                        .hasSize(1)
                        .any {
                            get { fileName }.isEqualTo("testSuite1 FailedTestCase 2.png")
                            get { attachmentType }.isEqualTo(AttachmentType.IMAGE)
                        }
                }
            }
        }
    }

    @Test
    fun `should include screenshot and video attachments when test case has them`() {
        val publicId = randomPublicId()

        attachmentsEnabled = true

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/${publicId.id}/cases/failed") {
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(
                        TestSuiteData(
                            "testSuite1",
                            listOf("testSuite1PassedTestCase1", "testSuite1PassedTestCase2"),
                            listOf("testSuite1 FailedTestCase 1", "testSuite1 FailedTestCase 2"),
                            listOf(),
                            "testSuite1.spec.js"
                        ),
                    )
                )

                testRunDBGenerator.addAttachment(publicId, "screenshot-1", "testSuite1 FailedTestCase 1.png")
                testRunDBGenerator.addAttachment(publicId, "video-1", "testSuite1.spec.js.mp4")
                testRunDBGenerator.addAttachment(publicId, "screenshot-2", "testSuite1 FailedTestCase 2.png")
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val responseContent = response.content
                assertNotNull(responseContent)
                val failedTestCases: List<TestCase> = objectMapper.readValue(responseContent)

                expectThat(failedTestCases)
                    .hasSize(2)

                val failedTestCase1 = failedTestCases.find { it.name == "testSuite1 FailedTestCase 1" }
                expectThat(failedTestCase1).isNotNull().and {
                    get { attachments }.isNotNull()
                        .hasSize(2)
                        .any {
                            get { fileName }.isEqualTo("testSuite1 FailedTestCase 1.png")
                            get { attachmentType }.isEqualTo(AttachmentType.IMAGE)
                        }
                        .any {
                            get { fileName }.isEqualTo("testSuite1.spec.js.mp4")
                            get { attachmentType }.isEqualTo(AttachmentType.VIDEO)
                        }
                }

                val failedTestCase2 = failedTestCases.find { it.name == "testSuite1 FailedTestCase 2" }
                expectThat(failedTestCase2).isNotNull().and {
                    get { attachments }
                        .isNotNull()
                        .hasSize(2)
                        .any {
                            get { fileName }.isEqualTo("testSuite1 FailedTestCase 2.png")
                            get { attachmentType }.isEqualTo(AttachmentType.IMAGE)
                        }
                        .any {
                            get { fileName }.isEqualTo("testSuite1.spec.js.mp4")
                            get { attachmentType }.isEqualTo(AttachmentType.VIDEO)
                        }
                }
            }
        }
    }
}
