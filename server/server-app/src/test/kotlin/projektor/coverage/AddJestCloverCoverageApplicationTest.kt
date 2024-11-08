package projektor.coverage

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.coverage.CoverageFiles
import projektor.server.api.coverage.CoverageStats
import projektor.server.example.coverage.CloverXmlLoader
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import kotlin.test.assertNotNull

class AddJestCloverCoverageApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should add Jest coverage to test run then get it`() =
        testSuspend {
            val publicId = randomPublicId()

            val reportXmlBytes = CloverXmlLoader().uiClover().toByteArray()

            testRunDBGenerator.createSimpleTestRun(publicId)

            val postResponse =
                testClient.post("/run/$publicId/coverage") {
                    setBody(reportXmlBytes)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
            expectThat(coverageRuns).hasSize(1)

            val getResponse = testClient.get("/run/$publicId/coverage/overall")
            expectThat(getResponse.status).isEqualTo(HttpStatusCode.OK)

            val overallStats = objectMapper.readValue(getResponse.bodyAsText(), CoverageStats::class.java)
            assertNotNull(overallStats)

            expectThat(overallStats.lineStat) {
                get { covered }.isEqualTo(924)
                get { missed }.isEqualTo(97)
                get { total }.isEqualTo(1021)
                get { coveredPercentage }.isEqualTo(BigDecimal("90.50"))
            }

            expectThat(overallStats.branchStat) {
                get { covered }.isEqualTo(158)
                get { missed }.isEqualTo(37)
                get { total }.isEqualTo(195)
                get { coveredPercentage }.isEqualTo(BigDecimal("81.03"))
            }

            expectThat(overallStats.statementStat) {
                get { covered }.isEqualTo(0)
                get { missed }.isEqualTo(0)
                get { total }.isEqualTo(0)
            }
        }

    @Test
    fun `should add Jest coverage and get its files`() =
        testSuspend {
            val publicId = randomPublicId()

            val reportXmlBytes = CloverXmlLoader().uiClover2().toByteArray()

            testRunDBGenerator.createSimpleTestRun(publicId)

            val postResponse =
                testClient.post("/run/$publicId/coverage") {
                    setBody(reportXmlBytes)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
            expectThat(coverageRuns).hasSize(1)

            val getResponse = testClient.get("/run/$publicId/coverage/All%20files/files")

            expectThat(getResponse.status).isEqualTo(HttpStatusCode.OK)

            val coverageFiles = objectMapper.readValue(getResponse.bodyAsText(), CoverageFiles::class.java)
            assertNotNull(coverageFiles)

            expectThat(coverageFiles.files).hasSize(97)
        }
}
