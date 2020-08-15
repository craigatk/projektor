package projektor.coverage

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import java.math.BigDecimal
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.coverage.CoverageStats
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

@KtorExperimentalAPI
@ExperimentalStdlibApi
class AddCoverageApplicationTest : ApplicationTestCase() {
    @Test
    fun `should add Jacoco coverage to test run then get it`() {
        val publicId = randomPublicId()

        val reportXmlBytes = JacocoXmlLoader().serverApp().toByteArray()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/coverage") {
                testRunDBGenerator.createTestRun(
                        publicId,
                        listOf(
                                TestSuiteData("testSuite1",
                                        listOf("testSuite1TestCase1"),
                                        listOf(),
                                        listOf()
                                )
                        )
                )

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
        }
    }
}
