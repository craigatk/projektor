package projektor.testsuite

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.TableField
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import projektor.database.generated.Tables
import projektor.database.generated.tables.records.TestSuiteRecord
import projektor.server.api.PublicId
import projektor.server.api.TestSuite
import projektor.server.api.TestSuiteOutput
import projektor.util.addPrefixToFields

class TestSuiteDatabaseRepository(private val dslContext: DSLContext) : TestSuiteRepository {
    private val testSuiteMapper = JdbcMapperFactory.newInstance()
        .addKeys("id", "test_cases_id", "test_cases_failure_id")
        .ignorePropertyNotFound()
        .newMapper(TestSuite::class.java)

    override suspend fun fetchTestSuite(testRunPublicId: PublicId, testSuiteIdx: Int): TestSuite? =
        withContext(Dispatchers.IO) {
            val resultSet = dslContext
                .select(Tables.TEST_SUITE.fields().toList())
                .select(addPrefixToFields("test_cases_", Tables.TEST_CASE.fields().toList()))
                .select(Tables.TEST_RUN.PUBLIC_ID.`as`("test_cases_public_id"))
                .select(Tables.TEST_SUITE.IDX.`as`("test_cases_test_suite_idx"))
                .select(Tables.TEST_RUN.CREATED_TIMESTAMP.`as`("test_cases_created_timestamp"))
                .select(addPrefixToFields("test_cases_failure_", Tables.TEST_FAILURE.fields().toList()))
                .select(Tables.TEST_SUITE_GROUP.fields().toList())
                .from(Tables.TEST_SUITE)
                .innerJoin(Tables.TEST_RUN).on(Tables.TEST_SUITE.TEST_RUN_ID.eq(Tables.TEST_RUN.ID))
                .leftOuterJoin(Tables.TEST_CASE).on(Tables.TEST_CASE.TEST_SUITE_ID.eq(Tables.TEST_SUITE.ID))
                .leftOuterJoin(Tables.TEST_FAILURE).on(Tables.TEST_FAILURE.TEST_CASE_ID.eq(Tables.TEST_CASE.ID))
                .leftOuterJoin(Tables.TEST_SUITE_GROUP).on(Tables.TEST_SUITE_GROUP.ID.eq(Tables.TEST_SUITE.TEST_SUITE_GROUP_ID))
                .where(Tables.TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id).and(Tables.TEST_SUITE.IDX.eq(testSuiteIdx)))
                .orderBy(Tables.TEST_SUITE.ID)
                .fetchResultSet()

            val testSuite: TestSuite? = resultSet.use {
                testSuiteMapper.stream(resultSet).findFirst().orElse(null)
            }

            testSuite
        }

    override suspend fun fetchTestSuites(testRunPublicId: PublicId, searchCriteria: TestSuiteSearchCriteria): List<TestSuite> =
        withContext(Dispatchers.IO) {
            val conditions = mutableListOf(Tables.TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id))

            if (searchCriteria.packageName != null) {
                conditions.add(Tables.TEST_SUITE.PACKAGE_NAME.eq(searchCriteria.packageName))
            }

            if (searchCriteria.failedOnly) {
                conditions.add(Tables.TEST_SUITE.FAILURE_COUNT.ge(1))
            }

            val testSuites = dslContext
                .select(Tables.TEST_SUITE.fields().toList())
                .from(Tables.TEST_SUITE)
                .innerJoin(Tables.TEST_RUN).on(Tables.TEST_SUITE.TEST_RUN_ID.eq(Tables.TEST_RUN.ID))
                .where(conditions)
                .fetchInto(TestSuite::class.java)

            testSuites
        }

    override suspend fun fetchTestSuiteSystemErr(testRunPublicId: PublicId, testSuiteIdx: Int): TestSuiteOutput =
        fetchTestSuiteOutputField(testRunPublicId, testSuiteIdx, Tables.TEST_SUITE.SYSTEM_ERR)

    override suspend fun fetchTestSuiteSystemOut(testRunPublicId: PublicId, testSuiteIdx: Int): TestSuiteOutput =
        fetchTestSuiteOutputField(testRunPublicId, testSuiteIdx, Tables.TEST_SUITE.SYSTEM_OUT)

    private suspend fun fetchTestSuiteOutputField(testRunPublicId: PublicId, testSuiteIdx: Int, field: TableField<TestSuiteRecord, String>) =
        withContext(Dispatchers.IO) {
            val outputValue = dslContext
                .select(field)
                .from(Tables.TEST_SUITE)
                .innerJoin(Tables.TEST_RUN).on(Tables.TEST_SUITE.TEST_RUN_ID.eq(Tables.TEST_RUN.ID))
                .where(Tables.TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id).and(Tables.TEST_SUITE.IDX.eq(testSuiteIdx)))
                .fetchOne(field)

            TestSuiteOutput(outputValue)
        }
}
