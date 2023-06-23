package projektor.repository.testrun

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL.count
import org.jooq.impl.DSL.min
import org.jooq.impl.DSL.noCondition
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import projektor.database.generated.Tables.GIT_METADATA
import projektor.database.generated.Tables.RESULTS_METADATA
import projektor.database.generated.Tables.TEST_CASE
import projektor.database.generated.Tables.TEST_RUN
import projektor.server.api.PublicId
import projektor.server.api.TestCase
import projektor.server.api.repository.BranchType
import projektor.server.api.repository.RepositoryTestRunTimeline
import projektor.server.api.repository.RepositoryTestRunTimelineEntry
import projektor.testcase.TestCaseDatabaseRepository
import projektor.testcase.TestCaseDatabaseRepository.Companion.selectTestCase
import kotlin.streams.toList

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
                .innerJoin(RESULTS_METADATA).on(TEST_RUN.ID.eq(RESULTS_METADATA.TEST_RUN_ID))
                .where(runInCIFromRepo(repoName, projectName))
                .orderBy(TEST_RUN.CREATED_TIMESTAMP.asc())
                .fetchResultSet()

            val timelineEntries: List<RepositoryTestRunTimelineEntry> = resultSet.use {
                timelineEntryMapper.stream(resultSet).toList()
            }

            if (timelineEntries.isNotEmpty()) RepositoryTestRunTimeline(timelineEntries) else null
        }

    override suspend fun fetchRepositoryFailingTestCases(
        repoName: String,
        projectName: String?,
        maxRuns: Int,
        branchType: BranchType
    ): List<TestCase> =
        withContext(Dispatchers.IO) {

            val resultSet = selectTestCase(dslContext)
                .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                .innerJoin(RESULTS_METADATA).on(TEST_RUN.ID.eq(RESULTS_METADATA.TEST_RUN_ID))
                .where(
                    runInCIFromRepo(repoName, projectName)
                        .and(TEST_CASE.PASSED.eq(false))
                        .and(withBranchType(branchType))
                        .and(
                            TEST_RUN.CREATED_TIMESTAMP.ge(
                                dslContext
                                    .select(min(TEST_RUN.`as`("x").CREATED_TIMESTAMP))
                                    .from(
                                        (
                                            dslContext.select(TEST_RUN.CREATED_TIMESTAMP)
                                                .from(TEST_RUN)
                                                .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                                                .innerJoin(RESULTS_METADATA).on(TEST_RUN.ID.eq(RESULTS_METADATA.TEST_RUN_ID))
                                                .where(runInCIFromRepo(repoName, projectName).and(withBranchType(branchType)))
                                                .orderBy(TEST_RUN.CREATED_TIMESTAMP.desc().nullsLast())
                                                .limit(maxRuns)
                                            ).asTable("x")
                                    )
                            )
                        )
                )
                .orderBy(TEST_RUN.CREATED_TIMESTAMP.desc().nullsLast())
                .fetchResultSet()

            val testCases = resultSet.use { TestCaseDatabaseRepository.testCaseMapper.stream(it).toList() }

            testCases
        }

    override suspend fun fetchRecentTestRunPublicIds(repoName: String, projectName: String?, maxRuns: Int): List<PublicId> =
        withContext(Dispatchers.IO) {
            dslContext
                .select(TEST_RUN.PUBLIC_ID)
                .from(TEST_RUN)
                .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                .innerJoin(RESULTS_METADATA).on(TEST_RUN.ID.eq(RESULTS_METADATA.TEST_RUN_ID))
                .where(runInCIFromRepo(repoName, projectName))
                .orderBy(TEST_RUN.CREATED_TIMESTAMP.desc().nullsLast())
                .limit(maxRuns)
                .fetchInto(String::class.java)
                .map { PublicId(it) }
        }

    override suspend fun fetchTestRunCount(repoName: String, projectName: String?): Long =
        withContext(Dispatchers.IO) {
            dslContext
                .select(count(TEST_RUN.ID))
                .from(TEST_RUN)
                .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                .innerJoin(RESULTS_METADATA).on(TEST_RUN.ID.eq(RESULTS_METADATA.TEST_RUN_ID))
                .where(runInCIFromRepo(repoName, projectName))
                .fetchOneInto(Long::class.java)!!
        }

    companion object {
        fun runInCIFromRepo(repoName: String, projectName: String?): Condition =
            GIT_METADATA.REPO_NAME.eq(repoName).let {
                if (projectName == null)
                    it.and(GIT_METADATA.PROJECT_NAME.isNull)
                else
                    it.and(GIT_METADATA.PROJECT_NAME.eq(projectName))
            }.and(RESULTS_METADATA.CI.eq(true))

        fun withBranchType(branchType: BranchType): Condition =
            when (branchType) {
                BranchType.MAINLINE -> GIT_METADATA.IS_MAIN_BRANCH.isTrue
                else -> noCondition()
            }
    }
}
