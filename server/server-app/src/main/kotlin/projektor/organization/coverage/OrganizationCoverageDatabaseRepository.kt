package projektor.organization.coverage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.impl.DSL.firstValue
import org.jooq.impl.DSL.partitionBy
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import projektor.database.generated.Tables.*
import kotlin.streams.toList

class OrganizationCoverageDatabaseRepository(private val dslContext: DSLContext) : OrganizationCoverageRepository {
    private val repositoryTestRunMapper =
        JdbcMapperFactory.newInstance()
            .addKeys("public_id")
            .ignorePropertyNotFound()
            .newMapper(RepositoryTestRun::class.java)

    override suspend fun findReposWithCoverage(orgName: String): List<RepositoryTestRun> =
        withContext(Dispatchers.IO) {
            // Every column that varies per run has to come from the most recent run, otherwise each
            // older run adds its own row to the DISTINCT results
            val mostRecentRunFirst =
                partitionBy(GIT_METADATA.REPO_NAME, GIT_METADATA.PROJECT_NAME)
                    .orderBy(TEST_RUN.CREATED_TIMESTAMP.desc(), TEST_RUN.ID.desc())

            val resultSet =
                dslContext.selectDistinct(
                    firstValue(TEST_RUN.PUBLIC_ID).over(mostRecentRunFirst).`as`("public_id"),
                    GIT_METADATA.REPO_NAME,
                    GIT_METADATA.PROJECT_NAME,
                    firstValue(GIT_METADATA.BRANCH_NAME).over(mostRecentRunFirst).`as`("branch_name"),
                    firstValue(TEST_RUN.CREATED_TIMESTAMP).over(mostRecentRunFirst).`as`("created_timestamp"),
                )
                    .from(GIT_METADATA)
                    .innerJoin(TEST_RUN).on(GIT_METADATA.TEST_RUN_ID.eq(TEST_RUN.ID))
                    .innerJoin(CODE_COVERAGE_RUN).on(TEST_RUN.PUBLIC_ID.eq(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID))
                    .innerJoin(CODE_COVERAGE_GROUP).on(CODE_COVERAGE_GROUP.CODE_COVERAGE_RUN_ID.eq(CODE_COVERAGE_RUN.ID))
                    .innerJoin(CODE_COVERAGE_STATS).on(CODE_COVERAGE_STATS.ID.eq(CODE_COVERAGE_GROUP.STATS_ID))
                    .where(
                        GIT_METADATA.ORG_NAME.eq(orgName)
                            .and(GIT_METADATA.IS_MAIN_BRANCH.eq(true))
                            .and(CODE_COVERAGE_STATS.LINE_COVERED.isNotNull),
                    )
                    .fetchResultSet()

            val repositoryTestRuns: List<RepositoryTestRun> =
                resultSet.use {
                    repositoryTestRunMapper.stream(resultSet).toList()
                }

            repositoryTestRuns
        }
}
