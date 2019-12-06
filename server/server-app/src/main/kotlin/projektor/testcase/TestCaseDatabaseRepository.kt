package projektor.testcase

import kotlin.streams.toList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import projektor.database.generated.Tables
import projektor.server.api.PublicId
import projektor.server.api.TestCase
import projektor.util.addPrefixToFields

class TestCaseDatabaseRepository(private val dslContext: DSLContext) : TestCaseRepository {
    private val testCaseMapper = JdbcMapperFactory.newInstance()
            .addKeys("id", "failure_id")
            .ignorePropertyNotFound()
            .newMapper(TestCase::class.java)

    override suspend fun fetchFailedTestCases(testRunPublicId: PublicId): List<TestCase> =
            withContext(Dispatchers.IO) {
                val resultSet = selectTestCase()
                        .where(Tables.TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id)
                                .and(Tables.TEST_CASE.PASSED.eq(false)))
                        .fetchResultSet()

                resultSet.use { testCaseMapper.stream(it).toList() }
            }

    override suspend fun fetchSlowTestCases(testRunPublicId: PublicId, limit: Int): List<TestCase> =
        withContext(Dispatchers.IO) {
            val resultSet = selectTestCase()
                    .where(Tables.TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id))
                    .orderBy(Tables.TEST_CASE.DURATION.desc())
                    .limit(limit)
                    .fetchResultSet()

            resultSet.use { testCaseMapper.stream(it).toList() }
        }

    override suspend fun fetchTestCase(testRunPublicId: PublicId, testSuiteIdx: Int, testCaseIdx: Int): TestCase? =
            withContext(Dispatchers.IO) {
                val resultSet = selectTestCase()
                        .where(Tables.TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id)
                                .and(Tables.TEST_SUITE.IDX.eq(testSuiteIdx))
                                .and(Tables.TEST_CASE.IDX.eq(testCaseIdx)))
                        .fetchResultSet()

                val testCase: TestCase? = resultSet.use {
                    testCaseMapper.stream(resultSet).findFirst().orElse(null)
                }

                testCase
            }

    private fun selectTestCase() =
            dslContext
                    .select(Tables.TEST_CASE.fields().toList())
                    .select(Tables.TEST_SUITE.IDX.`as`("test_suite_idx"))
                    .select(Tables.TEST_SUITE.HAS_SYSTEM_OUT.`as`("has_system_out"))
                    .select(Tables.TEST_SUITE.HAS_SYSTEM_ERR.`as`("has_system_err"))
                    .select(addPrefixToFields("failure_", Tables.TEST_FAILURE.fields().toList()))
                    .from(Tables.TEST_CASE)
                    .innerJoin(Tables.TEST_SUITE).on(Tables.TEST_SUITE.ID.eq(Tables.TEST_CASE.TEST_SUITE_ID))
                    .innerJoin(Tables.TEST_RUN).on(Tables.TEST_SUITE.TEST_RUN_ID.eq(Tables.TEST_RUN.ID))
                    .leftOuterJoin(Tables.TEST_FAILURE).on(Tables.TEST_FAILURE.TEST_CASE_ID.eq(Tables.TEST_CASE.ID))
}
