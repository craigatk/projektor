package projektor.incomingresults

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.database.generated.tables.pojos.CodeCoverageGroup
import projektor.database.generated.tables.pojos.CodeCoverageRun
import projektor.database.generated.tables.pojos.TestRun
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import projektor.server.api.PublicId
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.math.BigDecimal

class AppendCoverageApplicationTest : ApplicationTestCase() {
    @Test
    fun `should append three Jest test runs with coverage`() {
        val repoPart = RandomStringUtils.randomAlphabetic(12)

        val gitMetadata = GitMetadata()
        gitMetadata.repoName = "craigatk/$repoPart"
        gitMetadata.branchName = "main"
        gitMetadata.isMainBranch = true
        val metadata = ResultsMetadata()
        metadata.git = gitMetadata
        metadata.group = "B12"

        val coverageFilesTableRequestBody = GroupedResultsXmlLoader().resultsWithCoverage(
            listOf(resultsXmlLoader.jestCoverageFilesTable()),
            listOf(cloverXmlLoader.coverageFilesTable()),
            metadata
        )
        val coverageGraphRequestBody = GroupedResultsXmlLoader().resultsWithCoverage(
            listOf(resultsXmlLoader.jestCoverageGraph()),
            listOf(cloverXmlLoader.coverageGraph()),
            metadata
        )
        val coverageTableRequestBody = GroupedResultsXmlLoader().resultsWithCoverage(
            listOf(resultsXmlLoader.jestCoverageTable()),
            listOf(cloverXmlLoader.coverageTable()),
            metadata
        )

        lateinit var publicId: PublicId
        lateinit var testRun: TestRun
        lateinit var coverageRun: CodeCoverageRun
        lateinit var coverageGroup: CodeCoverageGroup

        /*
        ---------------------------|---------|----------|---------|---------|-------------------
        File                       | % Stmts | % Branch | % Funcs | % Lines | Uncovered Line #s
        ---------------------------|---------|----------|---------|---------|-------------------
        All files                  |   88.68 |    40.48 |      85 |   89.52 |
         Coverage                  |   91.14 |    51.85 |   88.24 |   91.14 |
          CoverageFilesTable.tsx   |      90 |    44.44 |   84.62 |      90 | 124,149-172
          CoverageGraph.tsx        |      95 |       60 |     100 |      95 | 82
          CoverageGraphImpl.tsx    |     100 |       75 |     100 |     100 | 22
          CoveragePercentage.tsx   |   78.57 |       25 |     100 |   78.57 | 42-61
         Link                      |      80 |        0 |       0 |     100 |
          CleanLink.tsx            |      80 |        0 |       0 |     100 | 11-16
         VersionControl            |   81.82 |    27.27 |     100 |   81.82 |
          GitHubFileLink.tsx       |   92.86 |       50 |     100 |   92.86 | 24
          VersionControlHelpers.ts |    62.5 |    22.22 |     100 |    62.5 | 10-16
        ---------------------------|---------|----------|---------|---------|-------------------
         */

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(coverageFilesTableRequestBody)
            }.apply {
                val initialSaveComplete = waitForTestRunSaveToComplete(response)
                publicId = initialSaveComplete.first
                testRun = initialSaveComplete.second

                await untilAsserted {
                    val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)
                    expectThat(testSuites).any {
                        get { className }.isEqualTo("CoverageFilesTable")
                        get { passingCount }.isEqualTo(2)
                    }
                }

                expectThat(testRun) {
                    get { totalPassingCount }.isEqualTo(2)
                }

                await untilAsserted {
                    expectThat(coverageRunDao.fetchByTestRunPublicId(publicId.id)).hasSize(1)
                }
                coverageRun = coverageRunDao.fetchByTestRunPublicId(publicId.id)[0]

                await untilAsserted {
                    expectThat(coverageGroupDao.fetchByCodeCoverageRunId(coverageRun.id)).hasSize(1)
                }
                coverageGroup = coverageGroupDao.fetchByCodeCoverageRunId(coverageRun.id)[0]
                expectThat(coverageGroup) {
                    get { name }.isEqualTo("All files")
                }

                val coverageFiles = runBlocking { coverageService.getCoverageGroupFiles(publicId, coverageGroup.name) }
                expectThat(coverageFiles).hasSize(7)
                expectThat(coverageFiles.find { it.fileName == "CoverageFilesTable.tsx" }).isNotNull().and {
                    get { stats.lineStat.coveredPercentage }.isEqualTo(BigDecimal("90.00"))
                    get { missedLines }.isEqualTo(arrayOf(124, 149, 172))
                }
                expectThat(coverageFiles.find { it.fileName == "CoverageGraph.tsx" }).isNotNull().and {
                    get { stats.lineStat.coveredPercentage }.isEqualTo(BigDecimal("95.00"))
                    get { missedLines }.isEqualTo(arrayOf(82))
                }
                expectThat(coverageFiles.find { it.fileName == "CoveragePercentage.tsx" }).isNotNull().and {
                    get { stats.lineStat.coveredPercentage }.isEqualTo(BigDecimal("78.57"))
                    get { missedLines }.isEqualTo(arrayOf(42, 43, 61))
                }
            }
        }

        /*
        -------------------------|---------|----------|---------|---------|-------------------
        File                     | % Stmts | % Branch | % Funcs | % Lines | Uncovered Line #s
        -------------------------|---------|----------|---------|---------|-------------------
        All files                |     100 |    86.36 |     100 |     100 |
         Coverage                |     100 |    94.44 |     100 |     100 |
          CoverageGraph.tsx      |     100 |      100 |     100 |     100 |
          CoverageGraphImpl.tsx  |     100 |       75 |     100 |     100 | 43
          CoveragePercentage.tsx |     100 |      100 |     100 |     100 |
         Link                    |     100 |       50 |     100 |     100 |
          CleanLink.tsx          |     100 |       50 |     100 |     100 | 11-16
        -------------------------|---------|----------|---------|---------|-------------------
         */

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(coverageGraphRequestBody)
            }.apply {
                val saveComplete = waitForTestRunSaveToComplete(response)
                val secondPublicId = saveComplete.first
                expectThat(secondPublicId).isEqualTo(publicId)

                val secondTestRun = saveComplete.second
                expectThat(secondTestRun.id).isEqualTo(testRun.id)

                await untilAsserted {
                    val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)
                    expectThat(testSuites).any {
                        get { className }.isEqualTo("CoverageGraph")
                        get { passingCount }.isEqualTo(7)
                    }
                }

                await untilAsserted {
                    val testRunSummary = testRunDao.fetchOneById(secondTestRun.id)
                    expectThat(testRunSummary) {
                        get { totalPassingCount }.isEqualTo(9)
                    }
                }

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
                }
            }
        }

        /*
        -------------------------|---------|----------|---------|---------|-------------------
        File                     | % Stmts | % Branch | % Funcs | % Lines | Uncovered Line #s
        -------------------------|---------|----------|---------|---------|-------------------
        All files                |   93.42 |    41.67 |   91.67 |   94.67 |
         Coverage                |   94.37 |       50 |     100 |   94.37 |
          CoverageGraph.tsx      |      95 |       60 |     100 |      95 | 82
          CoverageGraphImpl.tsx  |     100 |       50 |     100 |     100 | 22-43
          CoveragePercentage.tsx |   78.57 |       25 |     100 |   78.57 | 42-61
          CoverageTable.tsx      |     100 |       50 |     100 |     100 | 67
         Link                    |      80 |        0 |       0 |     100 |
          CleanLink.tsx          |      80 |        0 |       0 |     100 | 11-16
        -------------------------|---------|----------|---------|---------|-------------------
         */

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(coverageTableRequestBody)
            }.apply {
                val saveComplete = waitForTestRunSaveToComplete(response)
                val thirdPublicId = saveComplete.first
                expectThat(thirdPublicId).isEqualTo(publicId)

                val thirdTestRun = saveComplete.second

                await untilAsserted {
                    val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)
                    expectThat(testSuites).any {
                        get { className }.isEqualTo("CoverageTable")
                        get { passingCount }.isEqualTo(1)
                    }
                }

                await untilAsserted {
                    val testRunSummary = testRunDao.fetchOneById(thirdTestRun.id)
                    expectThat(testRunSummary) {
                        get { totalPassingCount }.isEqualTo(10)
                    }
                }

                await untilAsserted {
                    val coverageTableFile = runBlocking { coverageService.getCoverageGroupFiles(publicId, coverageGroup.name) }
                        .find { it.fileName == "CoverageTable.tsx" }

                    expectThat(coverageTableFile)
                        .isNotNull()
                        .get { stats.lineStat.coveredPercentage }.isEqualTo(BigDecimal("100.00"))
                }
            }
        }
    }
}
