package projektor.coverage

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
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

class AddCoverageCompressedApplicationTest : ApplicationTestCase() {
    @Test
    fun `should save compressed coverage results`() {
        val publicId = randomPublicId()

        val coverageXmlReport = JacocoXmlLoader().serverApp()
        val compressedBody = gzip(coverageXmlReport)

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/coverage") {
                testRunDBGenerator.createSimpleTestRun(publicId)

                addHeader(HttpHeaders.ContentType, "text/plain")
                addHeader(HttpHeaders.ContentEncoding, "gzip")
                setBody(compressedBody)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
                expectThat(coverageRuns).hasSize(1)

                expectThat(meterRegistry.counter("coverage_process_start").count()).isEqualTo(1.toDouble())
                expectThat(meterRegistry.counter("coverage_process_success").count()).isEqualTo(1.toDouble())
                expectThat(meterRegistry.counter("coverage_process_failure").count()).isEqualTo(0.toDouble())
                expectThat(meterRegistry.counter("coverage_parse_failure").count()).isEqualTo(0.toDouble())
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
