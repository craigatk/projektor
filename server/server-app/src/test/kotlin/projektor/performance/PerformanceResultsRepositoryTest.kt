package projektor.performance

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.incomingresults.model.PerformanceResult
import projektor.incomingresults.randomPublicId
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.math.BigDecimal

abstract class PerformanceResultsRepositoryTest : RepositoryTestCase() {
    @Test
    fun `should insert performance test results`() {
        val publicId = randomPublicId()

        testRunGenerator.createEmptyTestRun(publicId)

        val result =
            PerformanceResult(
                name = "perf.json",
                requestsPerSecond = BigDecimal("50.00"),
                requestCount = 4000,
                average = BigDecimal("18.022342"),
                maximum = BigDecimal("56.29193"),
                p95 = BigDecimal("45.02142"),
            )

        runBlocking {
            performanceResultsRepository.savePerformanceResults(
                publicId,
                result,
            )
        }

        val fetchedResultsList = runBlocking { performanceResultsRepository.fetchResults(publicId) }
        expectThat(fetchedResultsList).hasSize(1)

        val fetchResults = fetchedResultsList[0]

        expectThat(fetchResults) {
            get { name }.isEqualTo("perf.json")
            get { requestsPerSecond }.isEqualTo(BigDecimal("50.000"))
            get { requestCount }.isEqualTo(4000)
            get { average }.isEqualTo(BigDecimal("18.022"))
            get { maximum }.isEqualTo(BigDecimal("56.292"))
            get { p95 }.isEqualTo(BigDecimal("45.021"))
        }
    }
}

class PerformanceResultsRepositoryDatabaseTest : PerformanceResultsRepositoryTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class PerformanceResultsRepositoryObjectStoreTest : PerformanceResultsRepositoryTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
