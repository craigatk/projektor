package projektor.incomingresults

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.util.gzip
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import java.math.BigDecimal

class SaveResultsCompressedApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should uncompress gzipped results and save them`() =
        testSuspend {
            val requestBody = resultsXmlLoader.passing()
            val compressedBody = gzip(requestBody)

            val postResponse =
                testClient.post("/results") {
                    headers {
                        append(HttpHeaders.ContentType, "text/plain")
                        append(HttpHeaders.ContentEncoding, "gzip")
                    }
                    setBody(compressedBody)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            val (_, testRun) = waitForTestRunSaveToComplete(postResponse)

            expectThat(testRun.createdTimestamp).isNotNull()
            expectThat(testRun.totalTestCount).isEqualTo(1)
            expectThat(testRun.totalPassingCount).isEqualTo(1)
            expectThat(testRun.totalFailureCount).isEqualTo(0)
            expectThat(testRun.totalSkippedCount).isEqualTo(0)
            expectThat(testRun.passed).isTrue()
            expectThat(testRun.cumulativeDuration).isGreaterThan(BigDecimal.ZERO)
            expectThat(testRun.averageDuration).isGreaterThan(BigDecimal.ZERO)

            val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)
            expectThat(testSuites).hasSize(1)
        }
}
