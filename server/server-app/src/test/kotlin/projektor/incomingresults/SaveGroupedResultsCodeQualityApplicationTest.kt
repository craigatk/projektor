package projektor.incomingresults

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.util.gzip
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class SaveGroupedResultsCodeQualityApplicationTest : ApplicationTestCase() {

    @Test
    fun `should save results with code quality files`() {
        val codeQualityReports = (1..2).map { idx ->
            val codeQualityReport = projektor.parser.grouped.model.CodeQualityReport()
            codeQualityReport.contents = "Report contents $idx"
            codeQualityReport.fileName = "quality_$idx.txt"
            codeQualityReport
        }

        val requestBody = GroupedResultsXmlLoader().codeQualityResults(codeQualityReports)
        val compressedBody = gzip(requestBody)

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.ContentEncoding, "gzip")
                setBody(compressedBody)
            }.apply {
                val (_, testRun) = waitForTestRunSaveToComplete(response)

                val codeQualityReportDBs = codeQualityReportDao.fetchByTestRunId(testRun.id)

                expectThat(codeQualityReportDBs)
                    .hasSize(2)
                    .any {
                        get { idx }.isEqualTo(1)
                        get { contents }.isEqualTo("Report contents 1")
                        get { fileName }.isEqualTo("quality_1.txt")
                    }
                    .any {
                        get { idx }.isEqualTo(2)
                        get { contents }.isEqualTo("Report contents 2")
                        get { fileName }.isEqualTo("quality_2.txt")
                    }
            }
        }
    }
}
