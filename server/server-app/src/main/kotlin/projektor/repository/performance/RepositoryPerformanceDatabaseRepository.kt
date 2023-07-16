package projektor.repository.performance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import projektor.database.generated.Tables.GIT_METADATA
import projektor.database.generated.Tables.PERFORMANCE_RESULTS
import projektor.database.generated.Tables.TEST_RUN
import projektor.server.api.repository.performance.RepositoryPerformanceTestTimelineEntry
import kotlin.streams.toList

class RepositoryPerformanceDatabaseRepository(private val dslContext: DSLContext) : RepositoryPerformanceRepository {
    private val repositoryPerformanceTimelineEntryMapper = JdbcMapperFactory.newInstance()
        .addKeys("public_id")
        .ignorePropertyNotFound()
        .newMapper(RepositoryPerformanceTestTimelineEntry::class.java)

    override suspend fun fetchTestTimelineEntries(repoName: String, projectName: String?): List<RepositoryPerformanceTestTimelineEntry> =
        withContext(Dispatchers.IO) {
            val resultSet = dslContext.select(
                TEST_RUN.PUBLIC_ID,
                TEST_RUN.CREATED_TIMESTAMP,
                PERFORMANCE_RESULTS.NAME.`as`("performance_result.name"),
                PERFORMANCE_RESULTS.REQUESTS_PER_SECOND.`as`("performance_result.requests_per_second"),
                PERFORMANCE_RESULTS.REQUEST_COUNT.`as`("performance_result.request_count"),
                PERFORMANCE_RESULTS.AVERAGE.`as`("performance_result.average"),
                PERFORMANCE_RESULTS.P95.`as`("performance_result.p95"),
                PERFORMANCE_RESULTS.MAXIMUM.`as`("performance_result.maximum"),
            )
                .from(PERFORMANCE_RESULTS)
                .innerJoin(TEST_RUN).on(PERFORMANCE_RESULTS.TEST_RUN_ID.eq(TEST_RUN.ID))
                .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                .where(
                    GIT_METADATA.REPO_NAME.eq(repoName)
                        .let {
                            if (projectName == null)
                                it.and(GIT_METADATA.PROJECT_NAME.isNull)
                            else
                                it.and(GIT_METADATA.PROJECT_NAME.eq(projectName))
                        }
                )
                .orderBy(TEST_RUN.CREATED_TIMESTAMP.asc())
                .fetchResultSet()

            val timelineEntries: List<RepositoryPerformanceTestTimelineEntry> = resultSet.use {
                repositoryPerformanceTimelineEntryMapper.stream(resultSet).toList()
            }

            timelineEntries
        }
}
