package projektor.performance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import projektor.database.generated.tables.daos.PerformanceResultsDao
import projektor.database.generated.tables.pojos.PerformanceResults
import projektor.parser.performance.model.PerformanceResultsReport
import projektor.server.api.PublicId

class PerformanceResultsDatabaseRepository(private val dslContext: DSLContext) : PerformanceResultsRepository {
    private val performanceResultsDao = PerformanceResultsDao(dslContext.configuration())

    override suspend fun savePerformanceResults(
        testRunId: Long,
        publicId: PublicId,
        name: String,
        results: PerformanceResultsReport
    ) =
        withContext(Dispatchers.IO) {
            val performanceResults = PerformanceResults()
            performanceResults.testRunId = testRunId
            performanceResults.testRunPublicId = publicId.id
            performanceResults.name = name
            performanceResults.requestsPerSecond = results.requestStats.ratePerSecond
            performanceResults.requestCount = results.requestStats.count
            performanceResults.average = results.performanceStats.average
            performanceResults.maximum = results.performanceStats.maximum
            performanceResults.p95 = results.performanceStats.p95
            performanceResultsDao.insert(performanceResults)
        }
}
