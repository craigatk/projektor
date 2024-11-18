package projektor.testrun

import io.ktor.client.request.*
import io.ktor.client.request.headers
import io.ktor.client.statement.*
import io.ktor.http.*
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
    @Test
    fun `when gzip accept header included should compress test run response`() =
        projektorTestApplication {
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
                client.get("/run/$publicId") {
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
