package projektor.repository.testrun

import kotlin.streams.toList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import projektor.database.generated.Tables.*
import projektor.server.api.repository.RepositoryTestRunTimeline
import projektor.server.api.repository.RepositoryTestRunTimelineEntry

class RepositoryTestRunDatabaseRepository(private val dslContext: DSLContext) : RepositoryTestRunRepository {
    private val timelineEntryMapper = JdbcMapperFactory.newInstance()
            .addKeys("public_id")
            .ignorePropertyNotFound()
            .newMapper(RepositoryTestRunTimelineEntry::class.java)

    override suspend fun fetchRepositoryTestRunTimeline(repoName: String, projectName: String?): RepositoryTestRunTimeline? =
            withContext(Dispatchers.IO) {
                val resultSet = dslContext.select(
                        TEST_RUN.PUBLIC_ID,
                        TEST_RUN.CREATED_TIMESTAMP,
                        TEST_RUN.CUMULATIVE_DURATION,
                        TEST_RUN.WALL_CLOCK_DURATION,
                        TEST_RUN.PASSED,
                        TEST_RUN.TOTAL_TEST_COUNT
                )
                        .from(TEST_RUN)
                        .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                        .leftOuterJoin(RESULTS_METADATA).on(TEST_RUN.ID.eq(RESULTS_METADATA.TEST_RUN_ID))
                        .where(GIT_METADATA.REPO_NAME.eq(repoName).let {
                            if (projectName == null)
                                it.and(GIT_METADATA.PROJECT_NAME.isNull)
                            else
                                it.and(GIT_METADATA.PROJECT_NAME.eq(projectName))
                        }.and(RESULTS_METADATA.CI.eq(true)))
                        .orderBy(TEST_RUN.CREATED_TIMESTAMP.asc())
                        .fetchResultSet()

                val timelineEntries: List<RepositoryTestRunTimelineEntry> = resultSet.use {
                    timelineEntryMapper.stream(resultSet).toList()
                }

                if (timelineEntries.isNotEmpty()) RepositoryTestRunTimeline(timelineEntries) else null
            }
}
