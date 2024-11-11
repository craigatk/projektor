package projektor.quality

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.database.generated.tables.pojos.CodeQualityReport
import projektor.incomingresults.randomPublicId
import projektor.server.api.quality.CodeQualityReports
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

class CodeQualityReportApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `when code quality reports for test run should get them`() =
        testSuspend {
            val publicId = randomPublicId()

            val testRun = testRunDBGenerator.createEmptyTestRun(publicId)

            val codeQualityReports =
                (1..3).map { idx ->
                    val codeQualityReport = CodeQualityReport()

                    codeQualityReport.testRunId = testRun.id
                    codeQualityReport.contents = "Report contents $idx"
                    codeQualityReport.fileName = "code_quality_$idx.txt"
                    codeQualityReport.groupName = "group_$idx"
                    codeQualityReport.idx = idx

                    codeQualityReport
                }

            codeQualityReportDao.insert(codeQualityReports)

            val response = testClient.get("/run/$publicId/quality")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            val responseContent = response.bodyAsText()
            assertNotNull(responseContent)

            val codeQualityReportsResponse: CodeQualityReports = objectMapper.readValue(responseContent)
            assertNotNull(codeQualityReportsResponse)

            expectThat(codeQualityReportsResponse.reports)
                .hasSize(3)
                .any {
                    get { contents }.isEqualTo("Report contents 1")
                    get { fileName }.isEqualTo("code_quality_1.txt")
                    get { idx }.isEqualTo(1)
                }
                .any {
                    get { contents }.isEqualTo("Report contents 2")
                    get { fileName }.isEqualTo("code_quality_2.txt")
                    get { idx }.isEqualTo(2)
                }
                .any {
                    get { contents }.isEqualTo("Report contents 3")
                    get { fileName }.isEqualTo("code_quality_3.txt")
                    get { idx }.isEqualTo(3)
                }
        }

    @Test
    fun `when no code quality reports for test run should return 204`() =
        testSuspend {
            val publicId = randomPublicId()
            val otherPublicId = randomPublicId()

            val testRun = testRunDBGenerator.createEmptyTestRun(publicId)
            val otherTestRun = testRunDBGenerator.createEmptyTestRun(otherPublicId)

            val codeQualityReportForOtherTestRun = CodeQualityReport()

            codeQualityReportForOtherTestRun.testRunId = otherTestRun.id
            codeQualityReportForOtherTestRun.contents = "Report contents"
            codeQualityReportForOtherTestRun.fileName = "code_quality.txt"
            codeQualityReportForOtherTestRun.groupName = "group"

            codeQualityReportDao.insert(codeQualityReportForOtherTestRun)

            val response = testClient.get("/run/$publicId/quality")

            expectThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        }
}
