package projektor.quality

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
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

    @Test
    fun `when code quality reports for test run should get them`() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/quality") {

                val testRun = testRunDBGenerator.createEmptyTestRun(publicId)

                val codeQualityReports = (1..3).map { idx ->
                    val codeQualityReport = CodeQualityReport()

                    codeQualityReport.testRunId = testRun.id
                    codeQualityReport.contents = "Report contents $idx"
                    codeQualityReport.fileName = "code_quality_$idx.txt"
                    codeQualityReport.groupName = "group_$idx"
                    codeQualityReport.idx = idx

                    codeQualityReport
                }

                codeQualityReportDao.insert(codeQualityReports)
            }
        }.apply {
            expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
            val responseContent = response.content
            assertNotNull(responseContent)

            val codeQualityReports: CodeQualityReports = objectMapper.readValue(responseContent)
            assertNotNull(codeQualityReports)

            expectThat(codeQualityReports.reports)
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
    }

    @Test
    fun `when no code quality reports for test run should return 204`() {
        val publicId = randomPublicId()
        val otherPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/quality") {

                val testRun = testRunDBGenerator.createEmptyTestRun(publicId)
                val otherTestRun = testRunDBGenerator.createEmptyTestRun(otherPublicId)

                val codeQualityReportForOtherTestRun = CodeQualityReport()

                codeQualityReportForOtherTestRun.testRunId = otherTestRun.id
                codeQualityReportForOtherTestRun.contents = "Report contents"
                codeQualityReportForOtherTestRun.fileName = "code_quality.txt"
                codeQualityReportForOtherTestRun.groupName = "group"

                codeQualityReportDao.insert(codeQualityReportForOtherTestRun)
            }
        }.apply {
            expectThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
        }
    }
}
