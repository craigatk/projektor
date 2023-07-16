package projektor.organization.coverage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.impl.DSL.firstValue
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import projektor.database.generated.Tables.CODE_COVERAGE_RUN
import projektor.database.generated.Tables.GIT_METADATA
import projektor.database.generated.Tables.TEST_RUN
import kotlin.streams.toList

class OrganizationCoverageDatabaseRepository(private val dslContext: DSLContext) : OrganizationCoverageRepository {

    private val repositoryTestRunMapper = JdbcMapperFactory.newInstance()
        .addKeys("public_id")
        .ignorePropertyNotFound()
        .newMapper(RepositoryTestRun::class.java)

    override suspend fun findReposWithCoverage(orgName: String): List<RepositoryTestRun> =
        withContext(Dispatchers.IO) {
            val resultSet = dslContext.selectDistinct(
                firstValue(TEST_RUN.PUBLIC_ID).over().partitionBy(GIT_METADATA.REPO_NAME, GIT_METADATA.PROJECT_NAME).orderBy(TEST_RUN.CREATED_TIMESTAMP.desc()).`as`("public_id"),
                GIT_METADATA.REPO_NAME,
                GIT_METADATA.PROJECT_NAME
            )
                .from(GIT_METADATA)
                .innerJoin(TEST_RUN).on(GIT_METADATA.TEST_RUN_ID.eq(TEST_RUN.ID))
                .innerJoin(CODE_COVERAGE_RUN).on(TEST_RUN.PUBLIC_ID.eq(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID))
                .where(GIT_METADATA.ORG_NAME.eq(orgName).and(GIT_METADATA.IS_MAIN_BRANCH.eq(true)))
                .fetchResultSet()

            val repositoryTestRuns: List<RepositoryTestRun> = resultSet.use {
                repositoryTestRunMapper.stream(resultSet).toList()
            }

            repositoryTestRuns
        }
}
