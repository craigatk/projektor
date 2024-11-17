package projektor.performance

import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.database.generated.tables.daos.PerformanceResultsDao
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.PerformanceResult
import projektor.parser.grouped.model.ResultsMetadata
import projektor.server.example.performance.PerformanceResultsLoader
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.math.BigDecimal

class SavePerformanceResultsApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should save results with no test results and two performance results`() =
        projektorTestApplication {
            val gitMetadata = GitMetadata()
            gitMetadata.repoName = "craigatk/projektor"
            gitMetadata.branchName = "main"
            gitMetadata.isMainBranch = true
            val metadata = ResultsMetadata()
            metadata.git = gitMetadata

            val perfResult1 = PerformanceResult()
            perfResult1.name = "perf-1"
            perfResult1.resultsBlob = PerformanceResultsLoader().k6GetFailedTestCasesLarge()

            val perfResult2 = PerformanceResult()
            perfResult2.name = "perf-2"
            perfResult2.resultsBlob = PerformanceResultsLoader().k6GetRun()

            val requestBody =
                GroupedResultsXmlLoader().wrapPerformanceResultsInGroup(
                    listOf(perfResult1, perfResult2),
                    metadata,
                )

            val response = client.postGroupedResultsJSON(requestBody)

            val (publicId, _) = waitForTestRunSaveToComplete(response)

            val performanceResultsDao = PerformanceResultsDao(dslContext.configuration())

            val fetchedPerformanceResults = performanceResultsDao.fetchByTestRunPublicId(publicId.id)
            expectThat(fetchedPerformanceResults).hasSize(2)

            val fetchedPerfResult1 = fetchedPerformanceResults.find { it.name == "perf-1" }
            expectThat(fetchedPerfResult1).isNotNull().and {
                get { requestsPerSecond }.isEqualTo(BigDecimal("496.002"))
                get { requestCount }.isEqualTo(7675)
                get { average }.isEqualTo(BigDecimal("32.684"))
                get { maximum }.isEqualTo(BigDecimal("382.670"))
                get { p95 }.isEqualTo(BigDecimal("60.027"))
            }

            val fetchedPerfResult2 = fetchedPerformanceResults.find { it.name == "perf-2" }
            expectThat(fetchedPerfResult2).isNotNull().and {
                get { requestsPerSecond }.isEqualTo(BigDecimal("1019.924"))
                get { requestCount }.isEqualTo(66707)
                get { average }.isEqualTo(BigDecimal("11.072"))
                get { maximum }.isEqualTo(BigDecimal("410.530"))
                get { p95 }.isEqualTo(BigDecimal("22.294"))
            }
        }
}
