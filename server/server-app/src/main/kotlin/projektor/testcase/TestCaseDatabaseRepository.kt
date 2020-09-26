package projektor.testcase

import kotlin.streams.toList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectOnConditionStep
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import projektor.database.generated.Tables.*
import projektor.server.api.PublicId
import projektor.server.api.TestCase
import projektor.util.addPrefixToFields

class TestCaseDatabaseRepository(private val dslContext: DSLContext) : TestCaseRepository {

    override suspend fun fetchFailedTestCases(testRunPublicId: PublicId): List<TestCase> =
            withContext(Dispatchers.IO) {
                val resultSet = selectTestCase(dslContext)
                        .where(TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id)
                                .and(TEST_CASE.PASSED.eq(false)))
                        .orderBy(TEST_CASE.ID)
                        .fetchResultSet()

                resultSet.use { testCaseMapper.stream(it).toList() }
            }

    override suspend fun fetchSlowTestCases(testRunPublicId: PublicId, limit: Int): List<TestCase> =
        withContext(Dispatchers.IO) {
            val resultSet = selectTestCase(dslContext)
                    .where(TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id))
                    .orderBy(TEST_CASE.DURATION.desc())
                    .limit(limit)
                    .fetchResultSet()

            resultSet.use { testCaseMapper.stream(it).toList() }
        }

    override suspend fun fetchTestCase(testRunPublicId: PublicId, testSuiteIdx: Int, testCaseIdx: Int): TestCase? =
            withContext(Dispatchers.IO) {
                val resultSet = selectTestCase(dslContext)
                        .where(TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id)
                                .and(TEST_SUITE.IDX.eq(testSuiteIdx))
                                .and(TEST_CASE.IDX.eq(testCaseIdx)))
                        .orderBy(TEST_CASE.ID)
                        .fetchResultSet()

                val testCase: TestCase? = resultSet.use {
                    testCaseMapper.stream(resultSet).findFirst().orElse(null)
                }

                testCase
            }

    companion object {
        val testCaseMapper = JdbcMapperFactory.newInstance()
                .addKeys("id", "failure_id")
                .ignorePropertyNotFound()
                .newMapper(TestCase::class.java)

        fun selectTestCase(dslContext: DSLContext): SelectOnConditionStep<Record> =
                dslContext
                        .select(TEST_CASE.fields().toList())
                        .select(TEST_SUITE.IDX.`as`("test_suite_idx"))
                        .select(TEST_SUITE.HAS_SYSTEM_OUT.`as`("has_system_out"))
                        .select(TEST_SUITE.HAS_SYSTEM_ERR.`as`("has_system_err"))
                        .select(TEST_SUITE.PACKAGE_NAME.`as`("package_name"))
                        .select(TEST_RUN.PUBLIC_ID.`as`("public_id"))
                        .select(TEST_RUN.CREATED_TIMESTAMP.`as`("created_timestamp"))
                        .select(addPrefixToFields("failure_", TEST_FAILURE.fields().toList()))
                        .from(TEST_CASE)
                        .innerJoin(TEST_SUITE).on(TEST_SUITE.ID.eq(TEST_CASE.TEST_SUITE_ID))
                        .innerJoin(TEST_RUN).on(TEST_SUITE.TEST_RUN_ID.eq(TEST_RUN.ID))
                        .leftOuterJoin(TEST_FAILURE).on(TEST_FAILURE.TEST_CASE_ID.eq(TEST_CASE.ID))
    }
}
