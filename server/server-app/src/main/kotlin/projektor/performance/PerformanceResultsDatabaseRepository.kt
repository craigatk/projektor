package projektor.performance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import projektor.database.generated.Tables.PERFORMANCE_RESULTS
import projektor.database.generated.tables.daos.PerformanceResultsDao
import projektor.database.generated.tables.pojos.PerformanceResults
import projektor.server.api.PublicId
import projektor.server.api.performance.PerformanceResult
import projektor.incomingresults.model.PerformanceResult as IncomingPerformanceResult

class PerformanceResultsDatabaseRepository(private val dslContext: DSLContext) : PerformanceResultsRepository {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    private val performanceResultsDao = PerformanceResultsDao(dslContext.configuration())

    override suspend fun savePerformanceResults(
        testRunId: Long,
        publicId: PublicId,
        results: IncomingPerformanceResult,
    ): PerformanceResult =
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

            logger.info("Finished inserting performance result for test run $publicId")

            results.toApi()
        }

    override suspend fun fetchResults(publicId: PublicId): List<PerformanceResult> =
        withContext(Dispatchers.IO) {
            dslContext
                .selectFrom(PERFORMANCE_RESULTS)
                .where(PERFORMANCE_RESULTS.TEST_RUN_PUBLIC_ID.eq(publicId.id))
                .fetchInto(PerformanceResult::class.java)
        }
}
