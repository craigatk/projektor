package projektor.attachment

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import java.io.File
import kotlin.test.*
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.auth.AuthConfig
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@ExperimentalStdlibApi
class AddAttachmentTokenApplicationTest : ApplicationTestCase() {

    @Test
    fun `when token required and valid token included in header should add attachment`() {
        val validPublishToken = "publish12345"
        publishToken = validPublishToken

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

                addHeader(AuthConfig.PublishToken, validPublishToken)
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

    @Test
    fun `when token required and invalid token included in header should not add attachment`() {
        val validPublishToken = "publish12345"
        publishToken = validPublishToken

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

                addHeader(AuthConfig.PublishToken, "invalidPublishTOken")
                setBody(File("src/test/resources/test-attachment.txt").readBytes())
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.Unauthorized)
            }

            handleRequest(HttpMethod.Get, "/run/$publicId/attachments/test-attachment.txt") {
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }
        }
    }
}
