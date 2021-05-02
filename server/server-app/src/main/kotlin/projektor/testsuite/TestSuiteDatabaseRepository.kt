package projektor.testsuite

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectOnConditionStep
import org.jooq.TableField
import org.jooq.impl.DSL
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import projektor.database.generated.Tables
import projektor.database.generated.Tables.TEST_RUN
import projektor.database.generated.Tables.TEST_SUITE
import projektor.database.generated.tables.records.TestSuiteRecord
import projektor.server.api.PublicId
import projektor.server.api.TestOutput
import projektor.server.api.TestSuite
import projektor.util.addPrefixToFields
import kotlin.streams.toList

class TestSuiteDatabaseRepository(private val dslContext: DSLContext) : TestSuiteRepository {
    private val testSuiteMapper = JdbcMapperFactory.newInstance()
        .addKeys("id", "test_cases_id", "test_cases_failure_id")
        .ignorePropertyNotFound()
        .newMapper(TestSuite::class.java)

    override suspend fun fetchTestSuite(testRunPublicId: PublicId, testSuiteIdx: Int): TestSuite? =
        withContext(Dispatchers.IO) {
            val resultSet = selectTestSuite(dslContext)
                .where(TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id).and(TEST_SUITE.IDX.eq(testSuiteIdx)))
                .orderBy(TEST_SUITE.ID)
                .fetchResultSet()

            val testSuite: TestSuite? = resultSet.use {
                testSuiteMapper.stream(resultSet).findFirst().orElse(null)
            }

            testSuite
        }

    override suspend fun fetchTestSuites(testRunPublicId: PublicId, searchCriteria: TestSuiteSearchCriteria): List<TestSuite> =
        withContext(Dispatchers.IO) {
            val conditions = mutableListOf(TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id))

            if (searchCriteria.packageName != null) {
                conditions.add(TEST_SUITE.PACKAGE_NAME.eq(searchCriteria.packageName))
            }

            if (searchCriteria.failedOnly) {
                conditions.add(TEST_SUITE.FAILURE_COUNT.ge(1))
            }

            val testSuites = dslContext
                .select(TEST_SUITE.fields().toList())
                .from(TEST_SUITE)
                .innerJoin(TEST_RUN).on(TEST_SUITE.TEST_RUN_ID.eq(TEST_RUN.ID))
                .where(conditions)
                .fetchInto(TestSuite::class.java)

            testSuites
        }

    override suspend fun fetchTestSuitesWithCases(testRunPublicId: PublicId): List<TestSuite> =
        withContext(Dispatchers.IO) {
            val resultSet = selectTestSuite(dslContext)
                .where(TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id))
                .orderBy(TEST_SUITE.ID)
                .fetchResultSet()

            val testSuites = resultSet.use {
                testSuiteMapper.stream(resultSet).toList()
            }

            testSuites
        }

    override suspend fun fetchTestSuiteSystemErr(testRunPublicId: PublicId, testSuiteIdx: Int): TestOutput =
        fetchTestSuiteOutputField(testRunPublicId, testSuiteIdx, TEST_SUITE.SYSTEM_ERR)

    override suspend fun fetchTestSuiteSystemOut(testRunPublicId: PublicId, testSuiteIdx: Int): TestOutput =
        fetchTestSuiteOutputField(testRunPublicId, testSuiteIdx, TEST_SUITE.SYSTEM_OUT)

    private suspend fun fetchTestSuiteOutputField(testRunPublicId: PublicId, testSuiteIdx: Int, field: TableField<TestSuiteRecord, String>) =
        withContext(Dispatchers.IO) {
            val outputValue = dslContext
                .select(field)
                .from(TEST_SUITE)
                .innerJoin(TEST_RUN).on(TEST_SUITE.TEST_RUN_ID.eq(TEST_RUN.ID))
                .where(TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id).and(TEST_SUITE.IDX.eq(testSuiteIdx)))
                .fetchOne(field)

            TestOutput(outputValue)
        }

    override suspend fun fetchHighestTestSuiteIndex(testRunPublicId: PublicId): Int? =
        withContext(Dispatchers.IO) {
            dslContext
                .select(DSL.max(TEST_SUITE.IDX))
                .from(TEST_SUITE)
                .innerJoin(TEST_RUN).on(TEST_SUITE.TEST_RUN_ID.eq(TEST_RUN.ID))
                .where(TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id))
                .fetchOneInto(Int::class.java)
        }

    companion object {
        fun selectTestSuite(dslContext: DSLContext): SelectOnConditionStep<Record> =
            dslContext
                .select(TEST_SUITE.fields().toList())
                .select(Tables.TEST_CASE.addPrefixToFields("test_cases_"))
                .select(TEST_RUN.PUBLIC_ID.`as`("test_cases_public_id"))
                .select(TEST_SUITE.IDX.`as`("test_cases_test_suite_idx"))
                .select(TEST_RUN.CREATED_TIMESTAMP.`as`("test_cases_created_timestamp"))
                .select(Tables.TEST_FAILURE.addPrefixToFields("test_cases_failure_"))
                .select(Tables.TEST_SUITE_GROUP.fields().toList())
                .from(TEST_SUITE)
                .innerJoin(TEST_RUN).on(TEST_SUITE.TEST_RUN_ID.eq(TEST_RUN.ID))
                .leftOuterJoin(Tables.TEST_CASE).on(Tables.TEST_CASE.TEST_SUITE_ID.eq(TEST_SUITE.ID))
                .leftOuterJoin(Tables.TEST_FAILURE).on(Tables.TEST_FAILURE.TEST_CASE_ID.eq(Tables.TEST_CASE.ID))
                .leftOuterJoin(Tables.TEST_SUITE_GROUP).on(Tables.TEST_SUITE_GROUP.ID.eq(TEST_SUITE.TEST_SUITE_GROUP_ID))
    }
}
