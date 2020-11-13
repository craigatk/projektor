package projektor.repository.performance

import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import org.koin.test.inject
import projektor.DatabaseRepositoryTestCase
import projektor.incomingresults.model.PerformanceResult
import projektor.incomingresults.randomPublicId
import projektor.performance.PerformanceResultsRepository
import strikt.api.expectThat
import strikt.assertions.hasSize
import java.math.BigDecimal

class RepositoryPerformanceDatabaseRepositoryTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should fetch from any branch, not just the main one`() {
        val repositoryPerformanceRepository: RepositoryPerformanceRepository by inject()

        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"

        val publicId = randomPublicId()

        val testRunDB = testRunDBGenerator.createSimpleTestRun(publicId)
        testRunDBGenerator.addResultsMetadata(testRunDB, false)
        testRunDBGenerator.addGitMetadata(testRunDB, repoName, false, null, null)

        val savedPerfResult1 = PerformanceResult(
            name = "perf1",
            requestsPerSecond = BigDecimal("90.00"),
            requestCount = 6000,
            average = BigDecimal("20.67"),
            maximum = BigDecimal("42.88"),
            p95 = BigDecimal("30.48")
        )
        val performanceResultsRepository: PerformanceResultsRepository by inject()
        runBlocking { performanceResultsRepository.savePerformanceResults(testRunDB.id, publicId, savedPerfResult1) }

        val entries = runBlocking { repositoryPerformanceRepository.fetchTestTimelineEntries(repoName, null) }

        expectThat(entries).hasSize(1)
    }
}
