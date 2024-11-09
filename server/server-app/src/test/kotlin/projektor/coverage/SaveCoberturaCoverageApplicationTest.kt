package projektor.coverage

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.test.dispatcher.*
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
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should save Cobertura coverage and test results`() =
        testSuspend {
            val requestBody =
                GroupedResultsXmlLoader().resultsWithCoverage(
                    resultsXmls = listOf(ResultsXmlLoader().jestUi()),
                    coverageXmls = listOf(CoberturaXmlLoader().uiCobertura()),
                )

            val postResponse =
                testClient.post("/groupedResults") {
                    headers {
                        append(HttpHeaders.ContentType, "application/json")
                    }
                    setBody(requestBody)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            val (publicId, _) = waitForTestRunSaveToComplete(postResponse)

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

    @Test
    fun `should save Cobertura coverage without branch field on line and test results`() =
        testSuspend {
            val requestBody =
                GroupedResultsXmlLoader().resultsWithCoverage(
                    resultsXmls = listOf(ResultsXmlLoader().jestUi()),
                    coverageXmls = listOf(CoberturaXmlLoader().noBranchCobertura()),
                )

            val postResponse =
                testClient.post("/groupedResults") {
                    headers {
                        append(HttpHeaders.ContentType, "application/json")
                    }
                    setBody(requestBody)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            val (publicId, _) = waitForTestRunSaveToComplete(postResponse)

            await untilAsserted {
                expectThat(coverageRunDao.fetchByTestRunPublicId(publicId.id)).hasSize(1)
            }

            val coverage = runBlocking { coverageService.getCoverage(publicId) }
            assertNotNull(coverage)
        }
}
