package projektor.repository.performance

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import org.koin.ktor.ext.get
import projektor.ApplicationTestCase
import projektor.incomingresults.model.PerformanceResult
import projektor.incomingresults.randomPublicId
import projektor.performance.PerformanceResultsRepository
import projektor.server.api.repository.performance.RepositoryPerformanceTimeline
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.math.BigDecimal
import kotlin.test.assertNotNull

class RepositoryPerformanceTimelineApplicationTest : ApplicationTestCase() {

    @Test
    fun `should fetch performance timeline for repository without project name`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"

        val firstRunPublicId = randomPublicId()
        val secondRunPublicId = randomPublicId()
        val thirdRunPublicId = randomPublicId()

        val testName = "perf-test"

        val firstPerfResultToSave = PerformanceResult(
            name = testName,
            requestsPerSecond = BigDecimal("80.000"),
            requestCount = 4000,
            average = BigDecimal("25.664"),
            p95 = BigDecimal("40.783"),
            maximum = BigDecimal("45.991")
        )

        val secondPerfResultToSave = PerformanceResult(
            name = testName,
            requestsPerSecond = BigDecimal("90.000"),
            requestCount = 6000,
            average = BigDecimal("20.670"),
            p95 = BigDecimal("30.483"),
            maximum = BigDecimal("42.889")
        )

        val thirdPerfResultToSave = PerformanceResult(
            name = testName,
            requestsPerSecond = BigDecimal("100.000"),
            requestCount = 7000,
            average = BigDecimal("22.672"),
            p95 = BigDecimal("35.482"),
            maximum = BigDecimal("44.881")
        )

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/performance/timeline") {
                val performanceResultsRepository: PerformanceResultsRepository = application.get()

                val firstTestRun = testRunDBGenerator.createEmptyTestRunInRepo(firstRunPublicId, repoName, true, null)
                runBlocking { performanceResultsRepository.savePerformanceResults(firstTestRun.id, firstRunPublicId, firstPerfResultToSave) }

                val secondTestRun = testRunDBGenerator.createEmptyTestRunInRepo(secondRunPublicId, repoName, true, null)
                runBlocking { performanceResultsRepository.savePerformanceResults(secondTestRun.id, secondRunPublicId, secondPerfResultToSave) }

                val thirdTestRun = testRunDBGenerator.createEmptyTestRunInRepo(thirdRunPublicId, repoName, true, null)
                runBlocking { performanceResultsRepository.savePerformanceResults(thirdTestRun.id, thirdRunPublicId, thirdPerfResultToSave) }
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val repositoryPerformanceTimeline = objectMapper.readValue(response.content, RepositoryPerformanceTimeline::class.java)
                assertNotNull(repositoryPerformanceTimeline)

                expectThat(repositoryPerformanceTimeline.testTimelines).hasSize(1)

                val testTimeline = repositoryPerformanceTimeline.testTimelines[0]
                expectThat(testTimeline) {
                    get { name }.isEqualTo(testName)
                    get { entries }.hasSize(3)
                }

                val firstTimeline = testTimeline.entries.find { it.publicId == firstRunPublicId.id }
                expectThat(firstTimeline).isNotNull().and {
                    get { performanceResult.requestsPerSecond }.isEqualTo(BigDecimal("80.000"))
                    get { performanceResult.requestCount }.isEqualTo(4000)
                    get { performanceResult.average }.isEqualTo(BigDecimal("25.664"))
                    get { performanceResult.p95 }.isEqualTo(BigDecimal("40.783"))
                    get { performanceResult.maximum }.isEqualTo(BigDecimal("45.991"))
                }

                val secondTimeline = testTimeline.entries.find { it.publicId == secondRunPublicId.id }
                expectThat(secondTimeline).isNotNull().and {
                    get { performanceResult.requestsPerSecond }.isEqualTo(BigDecimal("90.000"))
                    get { performanceResult.requestCount }.isEqualTo(6000)
                    get { performanceResult.average }.isEqualTo(BigDecimal("20.670"))
                    get { performanceResult.p95 }.isEqualTo(BigDecimal("30.483"))
                    get { performanceResult.maximum }.isEqualTo(BigDecimal("42.889"))
                }

                val thirdTimeline = testTimeline.entries.find { it.publicId == thirdRunPublicId.id }
                expectThat(thirdTimeline).isNotNull().and {
                    get { performanceResult.requestsPerSecond }.isEqualTo(BigDecimal("100.000"))
                    get { performanceResult.requestCount }.isEqualTo(7000)
                    get { performanceResult.average }.isEqualTo(BigDecimal("22.672"))
                    get { performanceResult.p95 }.isEqualTo(BigDecimal("35.482"))
                    get { performanceResult.maximum }.isEqualTo(BigDecimal("44.881"))
                }
            }
        }
    }

    @Test
    fun `should fetch performance timeline for repository with project name`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = "server-project"

        val firstRunPublicId = randomPublicId()
        val secondRunPublicId = randomPublicId()
        val thirdRunPublicId = randomPublicId()

        val testName = "perf-test"

        val firstPerfResultToSave = PerformanceResult(
            name = testName,
            requestsPerSecond = BigDecimal("80.000"),
            requestCount = 4000,
            average = BigDecimal("25.664"),
            p95 = BigDecimal("40.783"),
            maximum = BigDecimal("45.991")
        )

        val secondPerfResultToSave = PerformanceResult(
            name = testName,
            requestsPerSecond = BigDecimal("90.000"),
            requestCount = 6000,
            average = BigDecimal("20.670"),
            p95 = BigDecimal("30.483"),
            maximum = BigDecimal("42.889")
        )

        val thirdPerfResultToSave = PerformanceResult(
            name = testName,
            requestsPerSecond = BigDecimal("100.000"),
            requestCount = 7000,
            average = BigDecimal("22.672"),
            p95 = BigDecimal("35.482"),
            maximum = BigDecimal("44.881")
        )

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/project/$projectName/performance/timeline") {
                val performanceResultsRepository: PerformanceResultsRepository = application.get()

                val firstTestRun = testRunDBGenerator.createEmptyTestRunInRepo(firstRunPublicId, repoName, true, projectName)
                runBlocking { performanceResultsRepository.savePerformanceResults(firstTestRun.id, firstRunPublicId, firstPerfResultToSave) }

                val secondTestRun = testRunDBGenerator.createEmptyTestRunInRepo(secondRunPublicId, repoName, true, projectName)
                runBlocking { performanceResultsRepository.savePerformanceResults(secondTestRun.id, secondRunPublicId, secondPerfResultToSave) }

                val thirdTestRun = testRunDBGenerator.createEmptyTestRunInRepo(thirdRunPublicId, repoName, true, projectName)
                runBlocking { performanceResultsRepository.savePerformanceResults(thirdTestRun.id, thirdRunPublicId, thirdPerfResultToSave) }
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val repositoryPerformanceTimeline = objectMapper.readValue(response.content, RepositoryPerformanceTimeline::class.java)
                assertNotNull(repositoryPerformanceTimeline)

                expectThat(repositoryPerformanceTimeline.testTimelines).hasSize(1)

                val testTimeline = repositoryPerformanceTimeline.testTimelines[0]
                expectThat(testTimeline) {
                    get { name }.isEqualTo(testName)
                    get { entries }.hasSize(3)
                }

                val firstTimeline = testTimeline.entries.find { it.publicId == firstRunPublicId.id }
                expectThat(firstTimeline).isNotNull().and {
                    get { performanceResult.requestsPerSecond }.isEqualTo(BigDecimal("80.000"))
                    get { performanceResult.requestCount }.isEqualTo(4000)
                    get { performanceResult.average }.isEqualTo(BigDecimal("25.664"))
                    get { performanceResult.p95 }.isEqualTo(BigDecimal("40.783"))
                    get { performanceResult.maximum }.isEqualTo(BigDecimal("45.991"))
                }

                val secondTimeline = testTimeline.entries.find { it.publicId == secondRunPublicId.id }
                expectThat(secondTimeline).isNotNull().and {
                    get { performanceResult.requestsPerSecond }.isEqualTo(BigDecimal("90.000"))
                    get { performanceResult.requestCount }.isEqualTo(6000)
                    get { performanceResult.average }.isEqualTo(BigDecimal("20.670"))
                    get { performanceResult.p95 }.isEqualTo(BigDecimal("30.483"))
                    get { performanceResult.maximum }.isEqualTo(BigDecimal("42.889"))
                }

                val thirdTimeline = testTimeline.entries.find { it.publicId == thirdRunPublicId.id }
                expectThat(thirdTimeline).isNotNull().and {
                    get { performanceResult.requestsPerSecond }.isEqualTo(BigDecimal("100.000"))
                    get { performanceResult.requestCount }.isEqualTo(7000)
                    get { performanceResult.average }.isEqualTo(BigDecimal("22.672"))
                    get { performanceResult.p95 }.isEqualTo(BigDecimal("35.482"))
                    get { performanceResult.maximum }.isEqualTo(BigDecimal("44.881"))
                }
            }
        }
    }
}
