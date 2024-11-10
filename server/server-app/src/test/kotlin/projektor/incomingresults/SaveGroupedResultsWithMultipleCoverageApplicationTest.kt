package projektor.incomingresults

import io.ktor.test.dispatcher.*
import kotlinx.coroutines.runBlocking
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.math.BigDecimal

class SaveGroupedResultsWithMultipleCoverageApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should save grouped results with multiple coverage reports`() =
        testSuspend {
            val requestBody =
                GroupedResultsXmlLoader().resultsWithCoverage(
                    listOf(
                        resultsXmlLoader.jestCoverageFilesTable(),
                        resultsXmlLoader.jestCoverageGraph(),
                        resultsXmlLoader.jestCoverageTable(),
                    ),
                    listOf(
                        cloverXmlLoader.coverageFilesTable(),
                        cloverXmlLoader.coverageGraph(),
                        cloverXmlLoader.coverageTable(),
                    ),
                )

            val response = postGroupedResultsJSON(requestBody)

            val (publicId, _) = waitForTestRunSaveToComplete(response)

            await until { coverageRunDao.fetchByTestRunPublicId(publicId.id).size == 1 }

            val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
            expectThat(coverageRuns).hasSize(1)
            val coverageRun = coverageRuns[0]

            await untilAsserted {
                expectThat(coverageGroupDao.fetchByCodeCoverageRunId(coverageRun.id)).hasSize(1)
            }
            val coverageGroup = coverageGroupDao.fetchByCodeCoverageRunId(coverageRun.id)[0]

            await untilAsserted {
                val coverageFiles = runBlocking { coverageService.getCoverageGroupFiles(publicId, coverageGroup.name) }
                expectThat(coverageFiles.find { it.fileName == "CoverageFilesTable.tsx" }).isNotNull().and {
                    get { stats.lineStat.coveredPercentage }.isEqualTo(BigDecimal("90.00"))
                    get { missedLines }.isEqualTo(arrayOf(124, 149, 172))
                }
                expectThat(coverageFiles.find { it.fileName == "CoverageGraph.tsx" }).isNotNull().and {
                    get { stats.lineStat.coveredPercentage }.isEqualTo(BigDecimal("100.00"))
                    get { missedLines }.isEqualTo(arrayOf())
                }
                expectThat(coverageFiles.find { it.fileName == "CoveragePercentage.tsx" }).isNotNull().and {
                    get { stats.lineStat.coveredPercentage }.isEqualTo(BigDecimal("100.00"))
                    get { missedLines }.isEqualTo(arrayOf())
                }
                expectThat(coverageFiles.find { it.fileName == "CoverageTable.tsx" }).isNotNull().and {
                    get { stats.lineStat.coveredPercentage }.isEqualTo(BigDecimal("100.00"))
                    get { missedLines }.isEqualTo(arrayOf())
                }
            }
        }
}
