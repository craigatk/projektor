package projektor.asset

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import java.io.File
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestRun
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@KtorExperimentalAPI
@ExperimentalStdlibApi
class AddAssetApplicationTest : ApplicationTestCase() {
    @Test
    fun `should add asset to test run with valid key`() {
        val publicId = randomPublicId()
        assetStoreEnabled = true

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId") {
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
            }.apply {
                val responseRun = objectMapper.readValue(response.content, TestRun::class.java)
                assertNotNull(responseRun)

                expectThat(responseRun.id).isEqualTo(publicId.id)
            }

            handleRequest(HttpMethod.Post, "/run/$publicId/asset/test-asset.txt") {
                setBody(File("src/test/resources/test-asset.txt").readBytes())
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                expectThat(objectStoreClient.getObject(assetStoreConfig.bucketName, "test-asset.txt").readBytes().decodeToString()).isEqualTo("Here is a test asset file")
            }
        }
    }
}
