package projektor.incomingresults

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.util.gzip
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class SaveGroupedResultsCompressedApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should save compressed grouped test results`() =
        testSuspend {
            val requestBody = GroupedResultsXmlLoader().passingGroupedResults()
            val compressedBody = gzip(requestBody)

            val response =
                testClient.post("/groupedResults") {
                    headers {
                        append(HttpHeaders.ContentType, "application/json")
                        append(HttpHeaders.ContentEncoding, "gzip")
                    }
                    setBody(compressedBody)
                }
            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val (_, testRun) = waitForTestRunSaveToComplete(response)

            val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)
            expectThat(testSuites).hasSize(3)
        }
}
