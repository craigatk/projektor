package projektor.coverage

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.coverage.CoverageFiles
import projektor.server.api.coverage.CoverageStats
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import kotlin.test.assertNotNull

class AddJacocoCoverageApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should add Jacoco coverage to test run then get it`() =
        testSuspend {
            val publicId = randomPublicId()

            val reportXmlBytes = JacocoXmlLoader().serverApp().toByteArray()

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

            expectThat(overallStats.branchStat) {
                get { covered }.isEqualTo(191)
                get { missed }.isEqualTo(57)
                get { total }.isEqualTo(248)
                get { coveredPercentage }.isEqualTo(BigDecimal("77.02"))
            }

            expectThat(overallStats.lineStat) {
                get { covered }.isEqualTo(953)
                get { missed }.isEqualTo(25)
                get { total }.isEqualTo(978)
                get { coveredPercentage }.isEqualTo(BigDecimal("97.44"))
            }

            expectThat(overallStats.statementStat) {
                get { covered }.isEqualTo(8816)
                get { missed }.isEqualTo(335)
                get { total }.isEqualTo(9151)
                get { coveredPercentage }.isEqualTo(BigDecimal("96.34"))
            }
        }

    @Test
    fun `should add server-app Jacoco coverage report and get its files`() =
        testSuspend {
            val publicId = randomPublicId()
            val coverageGroup = "server-app"

            val reportXmlBytes = JacocoXmlLoader().serverApp().toByteArray()

            testRunDBGenerator.createSimpleTestRun(publicId)

            val postResponse =
                testClient.post("/run/$publicId/coverage") {
                    setBody(reportXmlBytes)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
            expectThat(coverageRuns).hasSize(1)

            val getResponse = testClient.get("/run/$publicId/coverage/$coverageGroup/files")
            expectThat(getResponse.status).isEqualTo(HttpStatusCode.OK)

            val coverageFiles = objectMapper.readValue(getResponse.bodyAsText(), CoverageFiles::class.java)
            assertNotNull(coverageFiles)

            expectThat(coverageFiles.files).hasSize(62)
        }
}
