package projektor.coverage

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
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
    fun `should save compressed coverage results`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            val coverageXmlReport = JacocoXmlLoader().serverApp()
            val compressedBody = gzip(coverageXmlReport)

            testRunDBGenerator.createSimpleTestRun(publicId)

            val postResponse =
                client.post("/run/$publicId/coverage") {
                    headers {
                        append(HttpHeaders.ContentType, "text/plain")
                        append(HttpHeaders.ContentEncoding, "gzip")
                    }
                    setBody(compressedBody)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
            expectThat(coverageRuns).hasSize(1)

            expectThat(meterRegistry.counter("coverage_process_start").count()).isEqualTo(1.toDouble())
            expectThat(meterRegistry.counter("coverage_process_success").count()).isEqualTo(1.toDouble())
            expectThat(meterRegistry.counter("coverage_process_failure").count()).isEqualTo(0.toDouble())
            expectThat(meterRegistry.counter("coverage_parse_failure").count()).isEqualTo(0.toDouble())

            val getResponse = client.get("/run/$publicId/coverage/overall")

            expectThat(getResponse.status).isEqualTo(HttpStatusCode.OK)

            val overallStats = objectMapper.readValue(getResponse.bodyAsText(), CoverageStats::class.java)
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
