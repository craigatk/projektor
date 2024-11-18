package projektor.performance

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.ktor.ext.get
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.performance.PerformanceResults
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.math.BigDecimal
import kotlin.test.assertNotNull
import projektor.incomingresults.model.PerformanceResult as IncomingPerformanceResult

class GetPerformanceResultsApplicationTest : ApplicationTestCase() {
    @Test
    fun `when performance results should return them`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            val savedPerfResult1 =
                IncomingPerformanceResult(
                    name = "perf1",
                    requestsPerSecond = BigDecimal("80.00"),
                    requestCount = 4000,
                    average = BigDecimal("25.66"),
                    maximum = BigDecimal("45.99"),
                    p95 = BigDecimal("40.78"),
                )

            val savedPerfResult2 =
                IncomingPerformanceResult(
                    name = "perf2",
                    requestsPerSecond = BigDecimal("90.00"),
                    requestCount = 6000,
                    average = BigDecimal("20.67"),
                    maximum = BigDecimal("42.88"),
                    p95 = BigDecimal("30.48"),
                )

            val testRun = testRunDBGenerator.createEmptyTestRun(publicId)
            val performanceResultsRepository: PerformanceResultsRepository = getApplication().get()

            runBlocking { performanceResultsRepository.savePerformanceResults(testRun.id, publicId, savedPerfResult1) }
            runBlocking { performanceResultsRepository.savePerformanceResults(testRun.id, publicId, savedPerfResult2) }

            val response = client.get("/run/$publicId/performance")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val performanceResults = objectMapper.readValue(response.bodyAsText(), PerformanceResults::class.java)
            assertNotNull(performanceResults)

            expectThat(performanceResults.results).hasSize(2)

            val perfResult1 = performanceResults.results.find { it.name == "perf1" }
            expectThat(perfResult1).isNotNull().and {
                get { requestsPerSecond }.isEqualTo(BigDecimal("80.000"))
                get { requestCount }.isEqualTo(4000)
                get { average }.isEqualTo(BigDecimal("25.660"))
                get { maximum }.isEqualTo(BigDecimal("45.990"))
                get { p95 }.isEqualTo(BigDecimal("40.780"))
            }

            val perfResult2 = performanceResults.results.find { it.name == "perf2" }
            expectThat(perfResult2).isNotNull().and {
                get { requestsPerSecond }.isEqualTo(BigDecimal("90.000"))
                get { requestCount }.isEqualTo(6000)
                get { average }.isEqualTo(BigDecimal("20.670"))
                get { maximum }.isEqualTo(BigDecimal("42.880"))
                get { p95 }.isEqualTo(BigDecimal("30.480"))
            }
        }

    @Test
    fun `when no performance results for test run should return 204`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            testRunDBGenerator.createSimpleTestRun(publicId)

            val response = client.get("/run/$publicId/performance")

            expectThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        }
}
