package projektor.incomingresults

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.CoverageFile
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.hasSize

@KtorExperimentalAPI
class SaveGroupedResultsWithCoverage : ApplicationTestCase() {

    @Test
    fun `should save grouped results with coverage`() {
        val incomingCoverageFile = CoverageFile()
        incomingCoverageFile.reportContents = JacocoXmlLoader().jacocoXmlParser()

        val requestBody = GroupedResultsXmlLoader().passingResultsWithCoverage(listOf(incomingCoverageFile))

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                val (publicId, _) = waitForTestRunSaveToComplete(response)

                await until { coverageRunDao.fetchByTestRunPublicId(publicId.id).size == 1 }

                val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
                expectThat(coverageRuns).hasSize(1)

                await until { coverageGroupDao.fetchByCodeCoverageRunId(coverageRuns[0].id).size == 1 }
            }
        }
    }

    @Test
    fun `when one invalid coverage file should save the others`() {
        val invalidCoverageFile = CoverageFile()
        invalidCoverageFile.reportContents = JacocoXmlLoader().serverAppInvalid()

        val validCoverageFile1 = CoverageFile()
        validCoverageFile1.reportContents = JacocoXmlLoader().jacocoXmlParser()

        val validCoverageFile2 = CoverageFile()
        validCoverageFile2.reportContents = JacocoXmlLoader().junitResultsParser()

        val requestBody = GroupedResultsXmlLoader().passingResultsWithCoverage(listOf(validCoverageFile1, invalidCoverageFile, validCoverageFile2))

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                val (publicId, _) = waitForTestRunSaveToComplete(response)

                await until { coverageRunDao.fetchByTestRunPublicId(publicId.id).size == 1 }

                val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
                expectThat(coverageRuns).hasSize(1)

                await until { coverageGroupDao.fetchByCodeCoverageRunId(coverageRuns[0].id).size == 2 }
            }
        }
    }
}
