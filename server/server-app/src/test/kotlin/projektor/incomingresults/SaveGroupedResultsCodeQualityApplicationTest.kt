package projektor.incomingresults

import io.ktor.client.request.*
import io.ktor.http.*
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
    fun `should save results with code quality files`() =
        projektorTestApplication {
            val codeQualityReports =
                (1..2).map { idx ->
                    val codeQualityReport = projektor.parser.grouped.model.CodeQualityReport()
                    codeQualityReport.contents = "Report contents $idx"
                    codeQualityReport.fileName = "quality_$idx.txt"
                    codeQualityReport
                }

            val requestBody = GroupedResultsXmlLoader().codeQualityResults(codeQualityReports)
            val compressedBody = gzip(requestBody)

            val response =
                client.post("/groupedResults") {
                    headers {
                        append(HttpHeaders.ContentType, "application/json")
                        append(HttpHeaders.ContentEncoding, "gzip")
                    }
                    setBody(compressedBody)
                }
            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
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
