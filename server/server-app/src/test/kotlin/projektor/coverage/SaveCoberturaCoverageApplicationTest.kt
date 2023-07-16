package projektor.coverage

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.server.example.coverage.CoberturaXmlLoader
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import kotlin.test.assertNotNull

class SaveCoberturaCoverageApplicationTest : ApplicationTestCase() {

    @Test
    fun `should save Cobertura coverage and test results`() {
        val requestBody = GroupedResultsXmlLoader().resultsWithCoverage(
            resultsXmls = listOf(ResultsXmlLoader().jestUi()),
            coverageXmls = listOf(CoberturaXmlLoader().uiCobertura())
        )

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                val (publicId, _) = waitForTestRunSaveToComplete(response)

                await untilAsserted {
                    expectThat(coverageRunDao.fetchByTestRunPublicId(publicId.id)).hasSize(1)
                }

                await untilAsserted {
                    val coverage = runBlocking { coverageService.getCoverage(publicId) }
                    assertNotNull(coverage)

                    expectThat(coverage.overallStats) {
                        get { lineStat.coveredPercentage }.isEqualTo(BigDecimal("91.30"))
                        get { branchStat.coveredPercentage }.isEqualTo(BigDecimal("81.47"))
                    }

                    expectThat(coverage.groups).hasSize(1)
                    val coverageGroup = coverage.groups[0]
                    expectThat(coverageGroup.name).isEqualTo("Coverage")
                }

                await untilAsserted {
                    val coverageFiles = runBlocking { coverageService.getCoverageGroupFiles(publicId, "Coverage") }
                    expectThat(coverageFiles).hasSize(113)
                }

                val coverageFiles = runBlocking { coverageService.getCoverageGroupFiles(publicId, "Coverage") }

                expectThat(coverageFiles)
                    .any {
                        get { fileName }.isEqualTo("Dashboard.tsx")
                        get { filePath }.isEqualTo("src/Dashboard/Dashboard.tsx")
                    }
                    .any {
                        get { fileName }.isEqualTo("TestRunMenuWrapper.tsx")
                        get { filePath }.isEqualTo("src/TestRun/TestRunMenuWrapper.tsx")
                    }
            }
        }
    }

    @Test
    fun `should save Cobertura coverage without branch field on line and test results`() {
        val requestBody = GroupedResultsXmlLoader().resultsWithCoverage(
            resultsXmls = listOf(ResultsXmlLoader().jestUi()),
            coverageXmls = listOf(CoberturaXmlLoader().noBranchCobertura())
        )

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                val (publicId, _) = waitForTestRunSaveToComplete(response)

                await untilAsserted {
                    expectThat(coverageRunDao.fetchByTestRunPublicId(publicId.id)).hasSize(1)
                }

                val coverage = runBlocking { coverageService.getCoverage(publicId) }
                assertNotNull(coverage)
            }
        }
    }
}
