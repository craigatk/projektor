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
            }
        }
    }
}
