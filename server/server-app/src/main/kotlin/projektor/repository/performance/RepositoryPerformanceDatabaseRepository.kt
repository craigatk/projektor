package projektor.repository.performance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import projektor.database.generated.Tables.GIT_METADATA
import projektor.database.generated.Tables.PERFORMANCE_RESULTS
import projektor.database.generated.Tables.TEST_RUN
import projektor.repository.testrun.RepositoryTestRunDatabaseRepository.Companion.withProjectName
import projektor.server.api.performance.PerformanceResult
import projektor.server.api.repository.performance.RepositoryPerformanceTestTimelineEntry
import projektor.util.toInstantInDefaultZone

class RepositoryPerformanceDatabaseRepository(private val dslContext: DSLContext) : RepositoryPerformanceRepository {
    override suspend fun fetchTestTimelineEntries(
        repoName: String,
        projectName: String?,
    ): List<RepositoryPerformanceTestTimelineEntry> =
        withContext(Dispatchers.IO) {
            dslContext.select(
                TEST_RUN.PUBLIC_ID,
                TEST_RUN.CREATED_TIMESTAMP,
                PERFORMANCE_RESULTS.NAME,
                PERFORMANCE_RESULTS.REQUESTS_PER_SECOND,
                PERFORMANCE_RESULTS.REQUEST_COUNT,
                PERFORMANCE_RESULTS.AVERAGE,
                PERFORMANCE_RESULTS.P95,
                PERFORMANCE_RESULTS.MAXIMUM,
            )
                .from(PERFORMANCE_RESULTS)
                .innerJoin(TEST_RUN).on(PERFORMANCE_RESULTS.TEST_RUN_ID.eq(TEST_RUN.ID))
                .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                .where(
                    GIT_METADATA.REPO_NAME.eq(repoName)
                        .and(withProjectName(projectName)),
                )
                .orderBy(TEST_RUN.CREATED_TIMESTAMP.asc())
                .fetch { record ->
                    RepositoryPerformanceTestTimelineEntry(
                        publicId = record.get(TEST_RUN.PUBLIC_ID),
                        createdTimestamp = record.get(TEST_RUN.CREATED_TIMESTAMP).toInstantInDefaultZone(),
                        performanceResult =
                            PerformanceResult(
                                name = record.get(PERFORMANCE_RESULTS.NAME),
                                requestsPerSecond = record.get(PERFORMANCE_RESULTS.REQUESTS_PER_SECOND),
                                requestCount = record.get(PERFORMANCE_RESULTS.REQUEST_COUNT),
                                average = record.get(PERFORMANCE_RESULTS.AVERAGE),
                                maximum = record.get(PERFORMANCE_RESULTS.MAXIMUM),
                                p95 = record.get(PERFORMANCE_RESULTS.P95),
                            ),
                    )
                }
        }
}
