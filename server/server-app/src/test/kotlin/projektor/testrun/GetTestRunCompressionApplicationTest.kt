package projektor.testrun

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestRun
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import ungzip

@KtorExperimentalAPI
class GetTestRunCompressionApplicationTest : ApplicationTestCase() {
    @Test
    fun `when gzip accept header included should compress test run response`() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId") {
                testRunDBGenerator.createTestRun(
                        publicId,
                        listOf(
                                TestSuiteData("testSuite1",
                                        listOf("testSuite1TestCase1", "testSuite1TestCase2"),
                                        listOf(),
                                        listOf()
                                ),
                                TestSuiteData("testSuite2",
                                        listOf("testSuite2TestCase1", "testSuite2TestCase2", "testSuite2TestCase3"),
                                        listOf(),
                                        listOf()
                                )
                        )
                )

                addHeader(HttpHeaders.AcceptEncoding, "gzip")
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val responseBytes = response.byteContent
                assertNotNull(responseBytes)

                val uncompressedResponse = ungzip(responseBytes)

                val responseRun = objectMapper.readValue(uncompressedResponse, TestRun::class.java)
                assertNotNull(responseRun)

                expectThat(responseRun.id).isEqualTo(publicId.id)

                val testSuites = responseRun.testSuites
                assertNotNull(testSuites)
                expectThat(testSuites).hasSize(2)
            }
        }
    }
}
