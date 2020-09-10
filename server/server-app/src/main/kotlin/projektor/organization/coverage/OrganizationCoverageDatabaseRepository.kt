package projektor.organization.coverage

import kotlin.streams.toList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import projektor.database.generated.Tables.*

class OrganizationCoverageDatabaseRepository(private val dslContext: DSLContext) : OrganizationCoverageRepository {

    private val repositoryTestRunMapper = JdbcMapperFactory.newInstance()
            .addKeys("public_id")
            .ignorePropertyNotFound()
            .newMapper(RepositoryTestRun::class.java)

    override suspend fun findReposWithCoverage(orgName: String): List<RepositoryTestRun> =
            withContext(Dispatchers.IO) {
                val resultSet = dslContext.select(
                                TEST_RUN.PUBLIC_ID,
                                GIT_METADATA.REPO_NAME
                        )
                        .from(GIT_METADATA)
                        .innerJoin(TEST_RUN).on(GIT_METADATA.TEST_RUN_ID.eq(TEST_RUN.ID))
                        .innerJoin(CODE_COVERAGE_RUN).on(TEST_RUN.PUBLIC_ID.eq(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID))
                        .where(GIT_METADATA.ORG_NAME.eq(orgName))
                        .fetchResultSet()

                val repositoryTestRuns: List<RepositoryTestRun> = resultSet.use {
                    repositoryTestRunMapper.stream(resultSet).toList()
                }

                repositoryTestRuns
            }
}
