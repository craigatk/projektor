package projektor.coverage

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.coverage.CoverageStats
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.util.gzip
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import kotlin.test.assertNotNull

@KtorExperimentalAPI
class AddCoverageCompressedApplicationTest : ApplicationTestCase() {
    @Test
    fun `should save compressed coverage results`() {
        val publicId = randomPublicId()

        val coverageXmlReport = JacocoXmlLoader().serverApp()
        val compressedBody = gzip(coverageXmlReport)

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/coverage") {
                testRunDBGenerator.createSimpleTestRun(publicId)

                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.ContentEncoding, "gzip")
                setBody(compressedBody)
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
                    get { coveredPercentage }.isEqualTo(BigDecimal("77.02"))
                }

                expectThat(overallStats.lineStat) {
                    get { coveredPercentage }.isEqualTo(BigDecimal("97.44"))
                }

                expectThat(overallStats.statementStat) {
                    get { coveredPercentage }.isEqualTo(BigDecimal("96.34"))
                }
            }
        }
    }
}
