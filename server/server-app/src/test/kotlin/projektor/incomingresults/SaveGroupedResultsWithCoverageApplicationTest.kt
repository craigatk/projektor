package projektor.incomingresults

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.CoverageFile
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.util.gzip
import strikt.api.expectThat
import strikt.assertions.get
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.math.BigDecimal

class SaveGroupedResultsWithCoverageApplicationTest : ApplicationTestCase() {

    @Test
    fun `should save grouped results with coverage`() {
        val incomingCoverageFile = CoverageFile()
        incomingCoverageFile.reportContents = JacocoXmlLoader().serverApp()

        val requestBody = GroupedResultsXmlLoader().passingResultsWithCoverage(listOf(incomingCoverageFile))
        val compressedBody = gzip(requestBody)

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.ContentEncoding, "gzip")
                setBody(compressedBody)
            }.apply {
                val (publicId, _) = waitForTestRunSaveToComplete(response)

                await until { coverageRunDao.fetchByTestRunPublicId(publicId.id).size == 1 }

                val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
                expectThat(coverageRuns).hasSize(1)
                val coverageRun = coverageRuns[0]

                await until { coverageGroupDao.fetchByCodeCoverageRunId(coverageRun.id).size == 1 }
                val coverageGroup = coverageGroupDao.fetchByCodeCoverageRunId(coverageRun.id)[0]

                await untilAsserted { expectThat(coverageFileDao.fetchByCodeCoverageRunId(coverageRun.id)).hasSize(62) }

                val coverageFiles = runBlocking { coverageService.getCoverageGroupFiles(publicId, coverageGroup.name) }
                expectThat(coverageFiles.find { it.fileName == "CleanupConfig.kt" }).isNotNull().and {
                    get { stats.lineStat.coveredPercentage }.isEqualTo(BigDecimal("100.00"))
                    get { stats.branchStat.coveredPercentage }.isEqualTo(BigDecimal("58.33"))
                }

                val coverage = runBlocking { coverageService.getCoverage(publicId) }
                expectThat(coverage).isNotNull().and {
                    get { overallStats.lineStat.coveredPercentage }.isEqualTo(BigDecimal("97.44"))
                    get { overallStats.branchStat.coveredPercentage }.isEqualTo(BigDecimal("77.02"))

                    get { groups }.hasSize(1)[0].and {
                        get { stats.lineStat.coveredPercentage }.isEqualTo(BigDecimal("97.44"))
                        get { stats.branchStat.coveredPercentage }.isEqualTo(BigDecimal("77.02"))
                    }
                }
            }
        }
    }
}
