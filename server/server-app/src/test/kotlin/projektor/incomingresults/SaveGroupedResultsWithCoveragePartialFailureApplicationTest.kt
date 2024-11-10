package projektor.incomingresults

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.test.dispatcher.*
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.CoverageFile
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.util.gzip
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class SaveGroupedResultsWithCoveragePartialFailureApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `when one invalid coverage file should save the others and record parsing failed metric`() =
        testSuspend {
            val invalidCoverageFile = CoverageFile()
            invalidCoverageFile.reportContents = JacocoXmlLoader().serverAppInvalid()

            val validCoverageFile1 = CoverageFile()
            validCoverageFile1.reportContents = JacocoXmlLoader().jacocoXmlParser()

            val validCoverageFile2 = CoverageFile()
            validCoverageFile2.reportContents = JacocoXmlLoader().junitResultsParser()

            val requestBody =
                GroupedResultsXmlLoader().passingResultsWithCoverage(
                    listOf(validCoverageFile1, invalidCoverageFile, validCoverageFile2),
                )
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

            val (publicId, _) = waitForTestRunSaveToComplete(response)

            await until { coverageRunDao.fetchByTestRunPublicId(publicId.id).size == 1 }

            val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
            expectThat(coverageRuns).hasSize(1)

            await until { coverageGroupDao.fetchByCodeCoverageRunId(coverageRuns[0].id).size == 2 }

            expectThat(meterRegistry.counter("coverage_parse_failure").count()).isEqualTo(1.toDouble())
        }
}
