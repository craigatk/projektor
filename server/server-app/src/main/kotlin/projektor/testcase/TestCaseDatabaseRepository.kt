package projektor.testcase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectOnConditionStep
import org.jooq.TableField
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import projektor.database.generated.Tables.TEST_CASE
import projektor.database.generated.Tables.TEST_FAILURE
import projektor.database.generated.Tables.TEST_RUN
import projektor.database.generated.Tables.TEST_SUITE
import projektor.database.generated.tables.records.TestCaseRecord
import projektor.server.api.PublicId
import projektor.server.api.TestCase
import projektor.server.api.TestOutput
import projektor.util.addPrefixToFields
import kotlin.streams.toList

class TestCaseDatabaseRepository(private val dslContext: DSLContext) : TestCaseRepository {

    override suspend fun fetchFailedTestCases(testRunPublicId: PublicId): List<TestCase> =
        withContext(Dispatchers.IO) {
            val resultSet = selectTestCase(dslContext)
                .where(
                    TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id)
                        .and(TEST_CASE.PASSED.eq(false))
                )
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
                .where(
                    TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id)
                        .and(TEST_SUITE.IDX.eq(testSuiteIdx))
                        .and(TEST_CASE.IDX.eq(testCaseIdx))
                )
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
                .select(TEST_CASE.HAS_SYSTEM_ERR.`as`("has_system_err_test_case"))
                .select(TEST_CASE.HAS_SYSTEM_OUT.`as`("has_system_out_test_case"))
                .select(TEST_SUITE.HAS_SYSTEM_ERR.`as`("has_system_err_test_suite"))
                .select(TEST_SUITE.HAS_SYSTEM_OUT.`as`("has_system_out_test_suite"))
                .select(TEST_SUITE.FILE_NAME.`as`("file_name"))
                .select(TEST_SUITE.PACKAGE_NAME.`as`("package_name"))
                .select(TEST_SUITE.CLASS_NAME.`as`("test_suite_name"))
                .select(TEST_RUN.PUBLIC_ID.`as`("public_id"))
                .select(TEST_RUN.CREATED_TIMESTAMP.`as`("created_timestamp"))
                .select(TEST_FAILURE.addPrefixToFields("failure_"))
                .from(TEST_CASE)
                .innerJoin(TEST_SUITE).on(TEST_SUITE.ID.eq(TEST_CASE.TEST_SUITE_ID))
                .innerJoin(TEST_RUN).on(TEST_SUITE.TEST_RUN_ID.eq(TEST_RUN.ID))
                .leftOuterJoin(TEST_FAILURE).on(TEST_FAILURE.TEST_CASE_ID.eq(TEST_CASE.ID))
    }

    override suspend fun fetchTestCaseSystemErr(publicId: PublicId, testSuiteIdx: Int, testCaseIdx: Int): TestOutput =
        fetchTestCaseOutputField(publicId, testSuiteIdx, testCaseIdx, TEST_CASE.SYSTEM_ERR)

    override suspend fun fetchTestCaseSystemOut(publicId: PublicId, testSuiteIdx: Int, testCaseIdx: Int): TestOutput =
        fetchTestCaseOutputField(publicId, testSuiteIdx, testCaseIdx, TEST_CASE.SYSTEM_OUT)

    private suspend fun fetchTestCaseOutputField(testRunPublicId: PublicId, testSuiteIdx: Int, testCaseIdx: Int, field: TableField<TestCaseRecord, String>) =
        withContext(Dispatchers.IO) {
            val outputValue = dslContext
                .select(field)
                .from(TEST_CASE)
                .innerJoin(TEST_SUITE).on(TEST_CASE.TEST_SUITE_ID.eq(TEST_SUITE.ID))
                .innerJoin(TEST_RUN).on(TEST_SUITE.TEST_RUN_ID.eq(TEST_RUN.ID))
                .where(
                    TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id)
                        .and(TEST_SUITE.IDX.eq(testSuiteIdx))
                        .and(TEST_CASE.IDX.eq(testCaseIdx))
                )
                .fetchOne(field)

            TestOutput(outputValue)
        }
}
