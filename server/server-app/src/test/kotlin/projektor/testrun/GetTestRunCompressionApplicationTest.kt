package projektor.testrun

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestRun
import projektor.util.ungzip
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import kotlin.test.assertNotNull

class GetTestRunCompressionApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `when gzip accept header included should compress test run response`() =
        testSuspend {
            val publicId = randomPublicId()

            val testSuiteCount = 200

            testRunDBGenerator.createTestRun(
                publicId,
                (1..testSuiteCount).map {
                    TestSuiteData(
                        "reallyLongNameForTestSuite$it",
                        listOf("testSuite${it}TestCase1", "testSuite${it}TestCase2"),
                        listOf(),
                        listOf(),
                    )
                },
            )

            val response =
                testClient.get("/run/$publicId") {
                    headers {
                        append(HttpHeaders.AcceptEncoding, "gzip")
                    }
                }

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            expectThat(response.headers[HttpHeaders.ContentLength]).isNull()

            val responseBytes = response.readBytes()
            assertNotNull(responseBytes)

            val uncompressedResponse = ungzip(responseBytes)

            val responseRun = objectMapper.readValue(uncompressedResponse, TestRun::class.java)
            assertNotNull(responseRun)

            expectThat(responseRun.id).isEqualTo(publicId.id)

            val testSuites = responseRun.testSuites
            assertNotNull(testSuites)
            expectThat(testSuites).hasSize(testSuiteCount)
        }
}
