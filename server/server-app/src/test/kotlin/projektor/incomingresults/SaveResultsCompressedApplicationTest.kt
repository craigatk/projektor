package projektor.incomingresults

import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.util.gzip
import strikt.api.expectThat
import strikt.assertions.*
import java.math.BigDecimal

class SaveResultsCompressedApplicationTest : ApplicationTestCase() {
    @Test
    fun `should uncompress gzipped results and save them`() {
        val requestBody = resultsXmlLoader.passing()
        val compressedBody = gzip(requestBody)

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/results") {
                addHeader(HttpHeaders.ContentType, "text/plain")
                addHeader(HttpHeaders.ContentEncoding, "gzip")
                setBody(compressedBody)
            }.apply {
                val (_, testRun) = waitForTestRunSaveToComplete(response)

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
    }
}
