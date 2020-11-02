package projektor.performance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import projektor.database.generated.tables.daos.PerformanceResultsDao
import projektor.database.generated.tables.pojos.PerformanceResults
import projektor.incomingresults.model.PerformanceResult
import projektor.server.api.PublicId

class PerformanceResultsDatabaseRepository(private val dslContext: DSLContext) : PerformanceResultsRepository {
    private val performanceResultsDao = PerformanceResultsDao(dslContext.configuration())

    override suspend fun savePerformanceResults(
        testRunId: Long,
        publicId: PublicId,
        results: PerformanceResult
    ) =
        withContext(Dispatchers.IO) {
            val performanceResults = PerformanceResults()
            performanceResults.testRunId = testRunId
            performanceResults.testRunPublicId = publicId.id
            performanceResults.name = results.name
            performanceResults.requestsPerSecond = results.requestsPerSecond
            performanceResults.requestCount = results.requestCount
            performanceResults.average = results.average
            performanceResults.maximum = results.maximum
            performanceResults.p95 = results.p95
            performanceResultsDao.insert(performanceResults)
        }
}
