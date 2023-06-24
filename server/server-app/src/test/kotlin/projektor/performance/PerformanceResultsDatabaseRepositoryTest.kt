package projektor.performance

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.database.generated.tables.daos.PerformanceResultsDao
import projektor.incomingresults.model.PerformanceResult
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.math.BigDecimal

class PerformanceResultsDatabaseRepositoryTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should insert performance test results`() {
        val performanceResultsDatabaseRepository = PerformanceResultsDatabaseRepository(dslContext)
        val performanceResultsDao = PerformanceResultsDao(dslContext.configuration())

        val publicId = randomPublicId()

        val testRun = testRunDBGenerator.createEmptyTestRun(publicId)

        val result = PerformanceResult(
            name = "perf.json",
            requestsPerSecond = BigDecimal("50.00"),
            requestCount = 4000,
            average = BigDecimal("18.022342"),
            maximum = BigDecimal("56.29193"),
            p95 = BigDecimal("45.02142")
        )

        runBlocking {
            performanceResultsDatabaseRepository.savePerformanceResults(
                testRun.id,
                publicId,
                result
            )
        }

        val fetchedResultsList = performanceResultsDao.fetchByTestRunPublicId(publicId.id)
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
