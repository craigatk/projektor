package projektor.attachment

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import java.io.File
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@KtorExperimentalAPI
@ExperimentalStdlibApi
class AddAttachmentApplicationTest : ApplicationTestCase() {
    @Test
    fun `should add attachment to test run then get it`() {
        val publicId = randomPublicId()
        assetStoreEnabled = true

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/attachments/test-attachment.txt") {
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

                setBody(File("src/test/resources/test-attachment.txt").readBytes())
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
            }

            handleRequest(HttpMethod.Get, "/run/$publicId/attachments/test-attachment.txt") {
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                expectThat(response.byteContent?.decodeToString()).isEqualTo("Here is a test attachment file")
            }
        }
    }
}
