package projektor.coverage

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.coverage.CoverageFiles
import projektor.server.api.coverage.CoverageStats
import projektor.server.example.coverage.JestXmlLoader
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import kotlin.test.assertNotNull

@KtorExperimentalAPI
class AddJestCloverCoverageApplicationTest : ApplicationTestCase() {
    @Test
    fun `should add Jest coverage to test run then get it`() {
        val publicId = randomPublicId()

        val reportXmlBytes = JestXmlLoader().uiClover().toByteArray()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/coverage") {
                testRunDBGenerator.createSimpleTestRun(publicId)

                setBody(reportXmlBytes)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
                expectThat(coverageRuns).hasSize(1)
            }

            handleRequest(HttpMethod.Get, "/run/$publicId/coverage/overall").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val overallStats = objectMapper.readValue(response.content, CoverageStats::class.java)
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
        }
    }

    @Test
    fun `should add Jest coverage and get its files`() {
        val publicId = randomPublicId()

        val reportXmlBytes = JestXmlLoader().uiClover2().toByteArray()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/coverage") {
                testRunDBGenerator.createSimpleTestRun(publicId)

                setBody(reportXmlBytes)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
                expectThat(coverageRuns).hasSize(1)
            }

            handleRequest(HttpMethod.Get, "/run/$publicId/coverage/All%20files/files").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverageFiles = objectMapper.readValue(response.content, CoverageFiles::class.java)
                assertNotNull(coverageFiles)

                expectThat(coverageFiles.files).hasSize(97)
            }
        }
    }
}
