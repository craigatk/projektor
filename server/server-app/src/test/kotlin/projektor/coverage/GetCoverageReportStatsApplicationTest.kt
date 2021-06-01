package projektor.coverage

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.coverage.Coverage
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import kotlin.test.assertNotNull

@ExperimentalStdlibApi
class GetCoverageReportStatsApplicationTest : ApplicationTestCase() {

    @Test
    fun `when one coverage report should get its stats`() {
        val publicId = randomPublicId()

        val reportXmlBytes = JacocoXmlLoader().serverApp().toByteArray()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/coverage") {
                testRunDBGenerator.createSimpleTestRun(publicId)

                setBody(reportXmlBytes)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
                expectThat(coverageRuns).hasSize(1)
            }

            handleRequest(HttpMethod.Get, "/run/$publicId/coverage").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverage = objectMapper.readValue(response.content, Coverage::class.java)
                assertNotNull(coverage)

                expectThat(coverage.overallStats.branchStat) {
                    get { covered }.isEqualTo(191)
                    get { missed }.isEqualTo(57)
                    get { total }.isEqualTo(248)
                    get { coveredPercentage }.isEqualTo(BigDecimal("77.02"))
                }

                expectThat(coverage.overallStats.lineStat) {
                    get { covered }.isEqualTo(953)
                    get { missed }.isEqualTo(25)
                    get { total }.isEqualTo(978)
                    get { coveredPercentage }.isEqualTo(BigDecimal("97.44"))
                }

                expectThat(coverage.overallStats.statementStat) {
                    get { covered }.isEqualTo(8816)
                    get { missed }.isEqualTo(335)
                    get { total }.isEqualTo(9151)
                    get { coveredPercentage }.isEqualTo(BigDecimal("96.34"))
                }

                expectThat(coverage.groups).hasSize(1)

                val coverageGroup = coverage.groups[0]

                expectThat(coverageGroup) {
                    get { name }.isEqualTo("server-app")
                }

                expectThat(coverageGroup.stats.branchStat) {
                    get { covered }.isEqualTo(191)
                    get { missed }.isEqualTo(57)
                    get { total }.isEqualTo(248)
                    get { coveredPercentage }.isEqualTo(BigDecimal("77.02"))
                }

                expectThat(coverageGroup.stats.lineStat) {
                    get { covered }.isEqualTo(953)
                    get { missed }.isEqualTo(25)
                    get { total }.isEqualTo(978)
                    get { coveredPercentage }.isEqualTo(BigDecimal("97.44"))
                }

                expectThat(coverageGroup.stats.statementStat) {
                    get { covered }.isEqualTo(8816)
                    get { missed }.isEqualTo(335)
                    get { total }.isEqualTo(9151)
                    get { coveredPercentage }.isEqualTo(BigDecimal("96.34"))
                }
            }
        }
    }

    @Test
    fun `when two coverage reports should get their stats`() {
        val publicId = randomPublicId()

        val serverAppReportXmlBytes = JacocoXmlLoader().serverApp().toByteArray()
        val junitResultsParserXmlBytes = JacocoXmlLoader().junitResultsParser().toByteArray()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/coverage") {
                testRunDBGenerator.createSimpleTestRun(publicId)

                setBody(serverAppReportXmlBytes)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
                expectThat(coverageRuns).hasSize(1)

                expectThat(coverageGroupDao.fetchByCodeCoverageRunId(coverageRuns[0].id)).hasSize(1)
            }

            handleRequest(HttpMethod.Post, "/run/$publicId/coverage") {
                setBody(junitResultsParserXmlBytes)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
                expectThat(coverageRuns).hasSize(1)

                expectThat(coverageGroupDao.fetchByCodeCoverageRunId(coverageRuns[0].id)).hasSize(2)
            }

            handleRequest(HttpMethod.Get, "/run/$publicId/coverage").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverage = objectMapper.readValue(response.content, Coverage::class.java)
                assertNotNull(coverage)

                expectThat(coverage.overallStats.branchStat) {
                    get { covered }.isEqualTo(191)
                    get { missed }.isEqualTo(57)
                    get { total }.isEqualTo(248)
                    get { coveredPercentage }.isEqualTo(BigDecimal("77.02"))
                }

                expectThat(coverage.overallStats.lineStat) {
                    get { covered }.isEqualTo(965)
                    get { missed }.isEqualTo(26)
                    get { total }.isEqualTo(991)
                    get { coveredPercentage }.isEqualTo(BigDecimal("97.38"))
                }

                expectThat(coverage.overallStats.statementStat) {
                    get { covered }.isEqualTo(8862)
                    get { missed }.isEqualTo(347)
                    get { total }.isEqualTo(9209)
                    get { coveredPercentage }.isEqualTo(BigDecimal("96.23"))
                }

                expectThat(coverage.groups).hasSize(2)

                val serverAppGroup = coverage.groups.find { it.name == "server-app" }
                assertNotNull(serverAppGroup)

                expectThat(serverAppGroup.stats.branchStat) {
                    get { covered }.isEqualTo(191)
                    get { missed }.isEqualTo(57)
                    get { total }.isEqualTo(248)
                    get { coveredPercentage }.isEqualTo(BigDecimal("77.02"))
                }

                expectThat(serverAppGroup.stats.lineStat) {
                    get { covered }.isEqualTo(953)
                    get { missed }.isEqualTo(25)
                    get { total }.isEqualTo(978)
                    get { coveredPercentage }.isEqualTo(BigDecimal("97.44"))
                }

                expectThat(serverAppGroup.stats.statementStat) {
                    get { covered }.isEqualTo(8816)
                    get { missed }.isEqualTo(335)
                    get { total }.isEqualTo(9151)
                    get { coveredPercentage }.isEqualTo(BigDecimal("96.34"))
                }

                val junitGroup = coverage.groups.find { it.name == "junit-results-parser" }
                assertNotNull(junitGroup)

                expectThat(junitGroup.stats.branchStat) {
                    get { covered }.isEqualTo(0)
                    get { missed }.isEqualTo(0)
                    get { total }.isEqualTo(0)
                    get { coveredPercentage }.isEqualTo(BigDecimal.ZERO)
                }

                expectThat(junitGroup.stats.lineStat) {
                    get { covered }.isEqualTo(12)
                    get { missed }.isEqualTo(1)
                    get { total }.isEqualTo(13)
                    get { coveredPercentage }.isEqualTo(BigDecimal("92.31"))
                }

                expectThat(junitGroup.stats.statementStat) {
                    get { covered }.isEqualTo(46)
                    get { missed }.isEqualTo(12)
                    get { total }.isEqualTo(58)
                    get { coveredPercentage }.isEqualTo(BigDecimal("79.31"))
                }
            }
        }
    }
}
