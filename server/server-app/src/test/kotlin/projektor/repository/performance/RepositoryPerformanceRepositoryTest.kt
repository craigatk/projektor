package projektor.repository.performance

import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.incomingresults.model.GitMetadata
import projektor.incomingresults.model.PerformanceResult
import projektor.incomingresults.randomPublicId
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import projektor.persistence.TestRunGenerator
import strikt.api.expectThat
import strikt.assertions.hasSize
import java.math.BigDecimal

abstract class RepositoryPerformanceRepositoryTest : RepositoryTestCase() {
    @Test
    fun `should fetch from any branch, not just the main one`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"

        val publicId = randomPublicId()

        testRunGenerator.createTestRun(
            publicId,
            listOf(TestRunGenerator.simpleTestSuite()),
            git = GitMetadata(repoName, null, null, false),
            ci = false,
        )

        val savedPerfResult1 =
            PerformanceResult(
                name = "perf1",
                requestsPerSecond = BigDecimal("90.00"),
                requestCount = 6000,
                average = BigDecimal("20.67"),
                maximum = BigDecimal("42.88"),
                p95 = BigDecimal("30.48"),
            )
        runBlocking { performanceResultsRepository.savePerformanceResults(publicId, savedPerfResult1) }

        val entries = runBlocking { repositoryPerformanceRepository.fetchTestTimelineEntries(repoName, null) }

        expectThat(entries).hasSize(1)
    }
}

class RepositoryPerformanceRepositoryDatabaseTest : RepositoryPerformanceRepositoryTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class RepositoryPerformanceRepositoryObjectStoreTest : RepositoryPerformanceRepositoryTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
