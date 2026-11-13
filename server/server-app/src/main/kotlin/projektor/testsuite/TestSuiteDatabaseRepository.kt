package projektor.testsuite

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectOnConditionStep
import org.jooq.TableField
import org.jooq.impl.DSL
import projektor.database.generated.Tables.TEST_CASE
import projektor.database.generated.Tables.TEST_FAILURE
import projektor.database.generated.Tables.TEST_RUN
import projektor.database.generated.Tables.TEST_SUITE
import projektor.database.generated.Tables.TEST_SUITE_GROUP
import projektor.database.generated.tables.records.TestSuiteRecord
import projektor.server.api.PublicId
import projektor.server.api.TestCase
import projektor.server.api.TestOutput
import projektor.server.api.TestSuite
import projektor.testcase.TestCaseDatabaseRepository.Companion.testCaseFieldsToSelect
import projektor.testcase.TestCaseDatabaseRepository.Companion.testFailureFieldsToSelect
import projektor.testcase.TestCaseDatabaseRepository.Companion.toNestedTestCase

class TestSuiteDatabaseRepository(private val dslContext: DSLContext) : TestSuiteRepository {
    override suspend fun fetchTestSuite(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
    ): TestSuite? =
        withContext(Dispatchers.IO) {
            selectTestSuite(dslContext)
                .where(TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id).and(TEST_SUITE.IDX.eq(testSuiteIdx)))
                .orderBy(TEST_SUITE.ID, TEST_CASE.ID)
                .fetch()
                .toTestSuitesWithCases()
                .firstOrNull()
        }

    override suspend fun fetchTestSuites(
        testRunPublicId: PublicId,
        searchCriteria: TestSuiteSearchCriteria,
    ): List<TestSuite> =
        withContext(Dispatchers.IO) {
            val conditions = mutableListOf(TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id))

            if (searchCriteria.packageName != null) {
                conditions.add(TEST_SUITE.PACKAGE_NAME.eq(searchCriteria.packageName))
            }

            if (searchCriteria.failedOnly) {
                conditions.add(TEST_SUITE.FAILURE_COUNT.ge(1))
            }

            selectTestSuite(dslContext)
                .where(conditions)
                .orderBy(TEST_SUITE.ID, TEST_CASE.ID)
                .fetch()
                .toTestSuitesWithCases()
        }

    override suspend fun fetchTestSuitesWithCases(testRunPublicId: PublicId): List<TestSuite> =
        withContext(Dispatchers.IO) {
            fetchTestSuitesWithCases(dslContext, testRunPublicId)
        }

    override suspend fun fetchTestSuiteSystemErr(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
    ): TestOutput = fetchTestSuiteOutputField(testRunPublicId, testSuiteIdx, TEST_SUITE.SYSTEM_ERR)

    override suspend fun fetchTestSuiteSystemOut(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
    ): TestOutput = fetchTestSuiteOutputField(testRunPublicId, testSuiteIdx, TEST_SUITE.SYSTEM_OUT)

    private suspend fun fetchTestSuiteOutputField(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
        field: TableField<TestSuiteRecord, String>,
    ) = withContext(Dispatchers.IO) {
        val outputValue =
            dslContext
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
        // Takes the DSLContext so it can be used inside a transaction
        fun fetchTestSuitesWithCases(
            dslContext: DSLContext,
            testRunPublicId: PublicId,
        ): List<TestSuite> =
            selectTestSuite(dslContext)
                .where(TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id))
                .orderBy(TEST_SUITE.ID, TEST_CASE.ID)
                .fetch()
                .toTestSuitesWithCases()

        val testSuiteFieldsToSelect = TEST_SUITE.fields().toList().minus(listOf(TEST_SUITE.SYSTEM_ERR, TEST_SUITE.SYSTEM_OUT))

        val testSuiteGroupFieldsToSelect = listOf(TEST_SUITE_GROUP.GROUP_NAME, TEST_SUITE_GROUP.GROUP_LABEL)

        /**
         * Selects test suites with one row per test case (and failure), to map with [toTestSuitesWithCases].
         */
        fun selectTestSuite(dslContext: DSLContext): SelectOnConditionStep<Record> =
            dslContext
                .select(testSuiteFieldsToSelect)
                .select(testSuiteGroupFieldsToSelect)
                .select(testCaseFieldsToSelect)
                .select(testFailureFieldsToSelect)
                .select(TEST_RUN.PUBLIC_ID, TEST_RUN.CREATED_TIMESTAMP)
                .from(TEST_SUITE)
                .innerJoin(TEST_RUN).on(TEST_SUITE.TEST_RUN_ID.eq(TEST_RUN.ID))
                .leftOuterJoin(TEST_CASE).on(TEST_CASE.TEST_SUITE_ID.eq(TEST_SUITE.ID))
                .leftOuterJoin(TEST_FAILURE).on(TEST_FAILURE.TEST_CASE_ID.eq(TEST_CASE.ID))
                .leftOuterJoin(TEST_SUITE_GROUP).on(TEST_SUITE_GROUP.ID.eq(TEST_SUITE.TEST_SUITE_GROUP_ID))

        /**
         * Groups the rows from [selectTestSuite] into test suites with their test cases, in row order.
         */
        fun List<Record>.toTestSuitesWithCases(): List<TestSuite> =
            groupBy { it.get(TEST_SUITE.ID) }
                .values
                .map { testSuiteRows ->
                    val testCases =
                        testSuiteRows
                            .filter { it.get(TEST_CASE.ID) != null }
                            .distinctBy { it.get(TEST_CASE.ID) }
                            .map { it.toNestedTestCase() }

                    testSuiteRows.first().toTestSuite(testCases)
                }

        /**
         * Requires the [testSuiteFieldsToSelect] and [testSuiteGroupFieldsToSelect].
         */
        fun Record.toTestSuite(testCases: List<TestCase>?) =
            TestSuite(
                idx = get(TEST_SUITE.IDX),
                fileName = get(TEST_SUITE.FILE_NAME),
                packageName = get(TEST_SUITE.PACKAGE_NAME),
                className = get(TEST_SUITE.CLASS_NAME),
                testCount = get(TEST_SUITE.TEST_COUNT),
                passingCount = get(TEST_SUITE.PASSING_COUNT),
                skippedCount = get(TEST_SUITE.SKIPPED_COUNT),
                failureCount = get(TEST_SUITE.FAILURE_COUNT),
                startTs = get(TEST_SUITE.START_TS),
                hostname = get(TEST_SUITE.HOSTNAME),
                duration = get(TEST_SUITE.DURATION),
                hasSystemOut = get(TEST_SUITE.HAS_SYSTEM_OUT),
                hasSystemErr = get(TEST_SUITE.HAS_SYSTEM_ERR),
                groupName = get(TEST_SUITE_GROUP.GROUP_NAME),
                groupLabel = get(TEST_SUITE_GROUP.GROUP_LABEL),
                testCases = testCases,
            )
    }
}
