package projektor.testcase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectOnConditionStep
import org.jooq.TableField
import org.jooq.impl.DSL
import projektor.database.generated.Tables.GIT_METADATA
import projektor.database.generated.Tables.RESULTS_METADATA
import projektor.database.generated.Tables.TEST_CASE
import projektor.database.generated.Tables.TEST_FAILURE
import projektor.database.generated.Tables.TEST_RUN
import projektor.database.generated.Tables.TEST_SUITE
import projektor.database.generated.tables.records.TestCaseRecord
import projektor.repository.testrun.RepositoryTestRunDatabaseRepository.Companion.withBranchName
import projektor.repository.testrun.RepositoryTestRunDatabaseRepository.Companion.withProjectName
import projektor.server.api.PublicId
import projektor.server.api.TestCase
import projektor.server.api.TestFailure
import projektor.server.api.TestOutput
import projektor.server.api.history.TestCaseHistoryEntry
import projektor.testcase.slow.TestCaseDurationBaseline
import projektor.testcase.slow.TestCaseDurationBaselines
import java.math.BigDecimal

class TestCaseDatabaseRepository(private val dslContext: DSLContext) : TestCaseRepository {
    override suspend fun fetchFailedTestCases(testRunPublicId: PublicId): List<TestCase> =
        withContext(Dispatchers.IO) {
            selectTestCase(dslContext)
                .where(
                    TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id)
                        .and(TEST_CASE.PASSED.eq(false)),
                )
                .orderBy(TEST_CASE.ID)
                .fetch { it.toTestCase() }
        }

    override suspend fun fetchSlowTestCases(
        testRunPublicId: PublicId,
        limit: Int,
    ): List<TestCase> =
        withContext(Dispatchers.IO) {
            selectTestCase(dslContext)
                .where(TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id))
                .orderBy(TEST_CASE.DURATION.desc())
                .limit(limit)
                .fetch { it.toTestCase() }
        }

    override suspend fun fetchTestCase(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
    ): TestCase? =
        withContext(Dispatchers.IO) {
            selectTestCase(dslContext)
                .where(
                    TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id)
                        .and(TEST_SUITE.IDX.eq(testSuiteIdx))
                        .and(TEST_CASE.IDX.eq(testCaseIdx)),
                )
                .orderBy(TEST_CASE.ID)
                .limit(1)
                .fetchOne { it.toTestCase() }
        }

    override suspend fun fetchTestCaseHistory(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
        maxRuns: Int,
    ): List<TestCaseHistoryEntry> =
        withContext(Dispatchers.IO) {
            val target =
                dslContext
                    .select(
                        TEST_CASE.NAME,
                        TEST_CASE.CLASS_NAME,
                        TEST_CASE.PACKAGE_NAME,
                        TEST_RUN.ID,
                        TEST_RUN.CREATED_TIMESTAMP,
                        GIT_METADATA.REPO_NAME,
                        GIT_METADATA.PROJECT_NAME,
                        GIT_METADATA.BRANCH_NAME,
                    )
                    .from(TEST_CASE)
                    .innerJoin(TEST_SUITE).on(TEST_CASE.TEST_SUITE_ID.eq(TEST_SUITE.ID))
                    .innerJoin(TEST_RUN).on(TEST_SUITE.TEST_RUN_ID.eq(TEST_RUN.ID))
                    .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                    .where(
                        TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id)
                            .and(TEST_SUITE.IDX.eq(testSuiteIdx))
                            .and(TEST_CASE.IDX.eq(testCaseIdx)),
                    )
                    .fetchOne()

            if (target?.get(GIT_METADATA.REPO_NAME) == null) return@withContext listOf()

            val recentRunIds = fetchRecentRunIds(target, maxRuns, includeTargetRun = true)

            val isRequestedTestCase = TEST_SUITE.IDX.eq(testSuiteIdx).and(TEST_CASE.IDX.eq(testCaseIdx))

            dslContext
                .select(
                    TEST_RUN.PUBLIC_ID,
                    TEST_SUITE.IDX,
                    TEST_CASE.IDX,
                    TEST_RUN.CREATED_TIMESTAMP,
                    TEST_CASE.PASSED,
                    TEST_CASE.SKIPPED,
                    TEST_CASE.DURATION,
                    GIT_METADATA.BRANCH_NAME,
                    GIT_METADATA.COMMIT_SHA,
                    GIT_METADATA.PULL_REQUEST_NUMBER,
                )
                .from(TEST_CASE)
                .innerJoin(TEST_SUITE).on(TEST_CASE.TEST_SUITE_ID.eq(TEST_SUITE.ID))
                .innerJoin(TEST_RUN).on(TEST_SUITE.TEST_RUN_ID.eq(TEST_RUN.ID))
                .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                .where(
                    TEST_RUN.ID.`in`(recentRunIds)
                        .and(TEST_CASE.NAME.eq(target.get(TEST_CASE.NAME)))
                        .and(TEST_CASE.CLASS_NAME.eq(target.get(TEST_CASE.CLASS_NAME)))
                        .and(TEST_CASE.PACKAGE_NAME.isNotDistinctFrom(target.get(TEST_CASE.PACKAGE_NAME))),
                )
                .orderBy(
                    TEST_RUN.CREATED_TIMESTAMP.desc(),
                    TEST_RUN.ID.desc(),
                    // If a run has the same test multiple times, prefer the one that was requested
                    DSL.`when`(isRequestedTestCase, 0).otherwise(1),
                    TEST_SUITE.IDX,
                    TEST_CASE.IDX,
                )
                .fetch { record ->
                    TestCaseHistoryEntry(
                        publicId = record.get(TEST_RUN.PUBLIC_ID),
                        testSuiteIdx = record.get(TEST_SUITE.IDX),
                        testCaseIdx = record.get(TEST_CASE.IDX),
                        createdTimestamp = record.get(TEST_RUN.CREATED_TIMESTAMP),
                        passed = record.get(TEST_CASE.PASSED),
                        skipped = record.get(TEST_CASE.SKIPPED),
                        duration = record.get(TEST_CASE.DURATION),
                        branchName = record.get(GIT_METADATA.BRANCH_NAME),
                        commitSha = record.get(GIT_METADATA.COMMIT_SHA),
                        pullRequestNumber = record.get(GIT_METADATA.PULL_REQUEST_NUMBER),
                    )
                }
                .distinctBy { it.publicId }
        }

    override suspend fun fetchTestCases(testRunPublicId: PublicId): List<TestCase> =
        withContext(Dispatchers.IO) {
            selectTestCase(dslContext)
                .where(TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id))
                .orderBy(TEST_CASE.ID)
                .fetch { it.toTestCase() }
        }

    override suspend fun fetchTestCaseDurationBaselines(
        testRunPublicId: PublicId,
        maxRuns: Int,
    ): TestCaseDurationBaselines =
        withContext(Dispatchers.IO) {
            val target =
                dslContext
                    .select(
                        TEST_RUN.ID,
                        TEST_RUN.CREATED_TIMESTAMP,
                        GIT_METADATA.REPO_NAME,
                        GIT_METADATA.PROJECT_NAME,
                        GIT_METADATA.BRANCH_NAME,
                    )
                    .from(TEST_RUN)
                    .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                    .where(TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id))
                    .fetchOne()

            if (target?.get(GIT_METADATA.REPO_NAME) == null) return@withContext TestCaseDurationBaselines(0, listOf())

            val baselineRunIds = fetchRecentRunIds(target, maxRuns, includeTargetRun = false)

            if (baselineRunIds.isEmpty()) return@withContext TestCaseDurationBaselines(0, listOf())

            val baseline =
                dslContext
                    .select(
                        TEST_CASE.PACKAGE_NAME,
                        TEST_CASE.CLASS_NAME,
                        TEST_CASE.NAME,
                        DSL.percentileCont(BigDecimal("0.5")).withinGroupOrderBy(TEST_CASE.DURATION).`as`("median_duration"),
                        DSL.countDistinct(TEST_RUN.ID).`as`("sample_count"),
                    )
                    .from(TEST_CASE)
                    .innerJoin(TEST_SUITE).on(TEST_CASE.TEST_SUITE_ID.eq(TEST_SUITE.ID))
                    .innerJoin(TEST_RUN).on(TEST_SUITE.TEST_RUN_ID.eq(TEST_RUN.ID))
                    .where(
                        TEST_RUN.ID.`in`(baselineRunIds)
                            .and(TEST_CASE.PASSED.isTrue)
                            .and(TEST_CASE.SKIPPED.isFalse)
                            .and(TEST_CASE.DURATION.isNotNull),
                    )
                    .groupBy(TEST_CASE.PACKAGE_NAME, TEST_CASE.CLASS_NAME, TEST_CASE.NAME)
                    .asTable("baseline")

            val baselinePackageName = baseline.field(TEST_CASE.PACKAGE_NAME)!!
            val baselineClassName = baseline.field(TEST_CASE.CLASS_NAME)!!
            val baselineName = baseline.field(TEST_CASE.NAME)!!
            val medianDuration = baseline.field("median_duration", BigDecimal::class.java)!!
            val sampleCount = baseline.field("sample_count", Int::class.java)!!

            // Match the baselines to the test cases in the requested run
            val baselines =
                dslContext
                    .select(TEST_SUITE.IDX, TEST_CASE.IDX, medianDuration, sampleCount)
                    .from(TEST_CASE)
                    .innerJoin(TEST_SUITE).on(TEST_CASE.TEST_SUITE_ID.eq(TEST_SUITE.ID))
                    .innerJoin(baseline).on(
                        TEST_CASE.PACKAGE_NAME.isNotDistinctFrom(baselinePackageName)
                            .and(TEST_CASE.CLASS_NAME.isNotDistinctFrom(baselineClassName))
                            .and(TEST_CASE.NAME.eq(baselineName)),
                    )
                    .where(TEST_SUITE.TEST_RUN_ID.eq(target.get(TEST_RUN.ID)))
                    .fetch { record ->
                        TestCaseDurationBaseline(
                            testSuiteIdx = record.get(TEST_SUITE.IDX),
                            testCaseIdx = record.get(TEST_CASE.IDX),
                            medianDuration = record.get(medianDuration),
                            sampleCount = record.get(sampleCount),
                        )
                    }

            TestCaseDurationBaselines(baselineRunIds.size, baselines)
        }

    /**
     * Most recent CI runs of the same repo, project, and branch as the target run, ordered most recent first.
     * The target run itself is included (even if it wasn't a CI run) only when [includeTargetRun] is true.
     */
    private fun fetchRecentRunIds(
        target: Record,
        maxRuns: Int,
        includeTargetRun: Boolean,
    ): List<Long> {
        val targetRunId = target.get(TEST_RUN.ID)
        val targetCreatedTimestamp = target.get(TEST_RUN.CREATED_TIMESTAMP)

        val ciCondition =
            if (includeTargetRun) {
                RESULTS_METADATA.CI.isTrue.or(TEST_RUN.ID.eq(targetRunId))
            } else {
                RESULTS_METADATA.CI.isTrue
            }
        val sameTimestampCondition =
            if (includeTargetRun) TEST_RUN.ID.le(targetRunId) else TEST_RUN.ID.lt(targetRunId)

        return dslContext
            .select(TEST_RUN.ID)
            .from(TEST_RUN)
            .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
            .leftOuterJoin(RESULTS_METADATA).on(TEST_RUN.ID.eq(RESULTS_METADATA.TEST_RUN_ID))
            .where(
                GIT_METADATA.REPO_NAME.eq(target.get(GIT_METADATA.REPO_NAME))
                    .and(withProjectName(target.get(GIT_METADATA.PROJECT_NAME)))
                    .and(withBranchName(target.get(GIT_METADATA.BRANCH_NAME)))
                    .and(ciCondition)
                    .and(
                        TEST_RUN.CREATED_TIMESTAMP.lt(targetCreatedTimestamp)
                            .or(TEST_RUN.CREATED_TIMESTAMP.eq(targetCreatedTimestamp).and(sameTimestampCondition)),
                    ),
            )
            .orderBy(TEST_RUN.CREATED_TIMESTAMP.desc(), TEST_RUN.ID.desc())
            .limit(maxRuns)
            .fetch(TEST_RUN.ID)
    }

    companion object {
        // The test case's own columns, without its potentially large output
        val testCaseFieldsToSelect =
            listOf(
                TEST_CASE.ID,
                TEST_CASE.IDX,
                TEST_CASE.NAME,
                TEST_CASE.PACKAGE_NAME,
                TEST_CASE.CLASS_NAME,
                TEST_CASE.DURATION,
                TEST_CASE.PASSED,
                TEST_CASE.SKIPPED,
                TEST_CASE.HAS_SYSTEM_OUT,
                TEST_CASE.HAS_SYSTEM_ERR,
            )

        val testFailureFieldsToSelect =
            listOf(
                TEST_FAILURE.ID,
                TEST_FAILURE.FAILURE_MESSAGE,
                TEST_FAILURE.FAILURE_TYPE,
                TEST_FAILURE.FAILURE_TEXT,
            )

        /**
         * Selects test cases along with their test suite data, to map with [toTestCase].
         */
        fun selectTestCase(dslContext: DSLContext): SelectOnConditionStep<Record> =
            dslContext
                .select(testCaseFieldsToSelect)
                .select(
                    TEST_SUITE.IDX,
                    TEST_SUITE.PACKAGE_NAME,
                    TEST_SUITE.CLASS_NAME,
                    TEST_SUITE.FILE_NAME,
                    TEST_SUITE.HAS_SYSTEM_OUT,
                    TEST_SUITE.HAS_SYSTEM_ERR,
                    TEST_RUN.PUBLIC_ID,
                    TEST_RUN.CREATED_TIMESTAMP,
                )
                .select(testFailureFieldsToSelect)
                .from(TEST_CASE)
                .innerJoin(TEST_SUITE).on(TEST_SUITE.ID.eq(TEST_CASE.TEST_SUITE_ID))
                .innerJoin(TEST_RUN).on(TEST_SUITE.TEST_RUN_ID.eq(TEST_RUN.ID))
                .leftOuterJoin(TEST_FAILURE).on(TEST_FAILURE.TEST_CASE_ID.eq(TEST_CASE.ID))

        /**
         * A test case fetched with [selectTestCase], which includes data from its test suite.
         */
        fun Record.toTestCase() =
            TestCase(
                idx = get(TEST_CASE.IDX),
                testSuiteIdx = get(TEST_SUITE.IDX),
                name = get(TEST_CASE.NAME),
                packageName = get(TEST_SUITE.PACKAGE_NAME),
                testSuiteName = get(TEST_SUITE.CLASS_NAME),
                className = get(TEST_CASE.CLASS_NAME),
                fileName = get(TEST_SUITE.FILE_NAME),
                duration = get(TEST_CASE.DURATION),
                passed = get(TEST_CASE.PASSED),
                skipped = get(TEST_CASE.SKIPPED),
                hasSystemOutTestCase = get(TEST_CASE.HAS_SYSTEM_OUT) ?: false,
                hasSystemErrTestCase = get(TEST_CASE.HAS_SYSTEM_ERR) ?: false,
                hasSystemOutTestSuite = get(TEST_SUITE.HAS_SYSTEM_OUT),
                hasSystemErrTestSuite = get(TEST_SUITE.HAS_SYSTEM_ERR),
                publicId = get(TEST_RUN.PUBLIC_ID),
                createdTimestamp = get(TEST_RUN.CREATED_TIMESTAMP),
                failure = toTestFailure(),
            )

        /**
         * A test case nested in a test suite, which only includes the test case's own data
         * (its own output flags, but not its test suite's). Requires the [testCaseFieldsToSelect],
         * [testFailureFieldsToSelect], test suite index, and test run public ID and created timestamp.
         */
        fun Record.toNestedTestCase() =
            TestCase(
                idx = get(TEST_CASE.IDX),
                testSuiteIdx = get(TEST_SUITE.IDX),
                name = get(TEST_CASE.NAME),
                packageName = get(TEST_CASE.PACKAGE_NAME),
                testSuiteName = null,
                className = get(TEST_CASE.CLASS_NAME),
                fileName = null,
                duration = get(TEST_CASE.DURATION),
                passed = get(TEST_CASE.PASSED),
                skipped = get(TEST_CASE.SKIPPED),
                hasSystemOutTestCase = get(TEST_CASE.HAS_SYSTEM_OUT) ?: false,
                hasSystemErrTestCase = get(TEST_CASE.HAS_SYSTEM_ERR) ?: false,
                hasSystemOutTestSuite = false,
                hasSystemErrTestSuite = false,
                publicId = get(TEST_RUN.PUBLIC_ID),
                createdTimestamp = get(TEST_RUN.CREATED_TIMESTAMP),
                failure = toTestFailure(),
            )

        // Null when the left join to the test failure didn't find one
        private fun Record.toTestFailure(): TestFailure? =
            if (get(TEST_FAILURE.ID) != null) {
                TestFailure(
                    failureMessage = get(TEST_FAILURE.FAILURE_MESSAGE),
                    failureType = get(TEST_FAILURE.FAILURE_TYPE),
                    failureText = get(TEST_FAILURE.FAILURE_TEXT),
                )
            } else {
                null
            }
    }

    override suspend fun fetchTestCaseSystemErr(
        publicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
    ): TestOutput = fetchTestCaseOutputField(publicId, testSuiteIdx, testCaseIdx, TEST_CASE.SYSTEM_ERR)

    override suspend fun fetchTestCaseSystemOut(
        publicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
    ): TestOutput = fetchTestCaseOutputField(publicId, testSuiteIdx, testCaseIdx, TEST_CASE.SYSTEM_OUT)

    private suspend fun fetchTestCaseOutputField(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
        field: TableField<TestCaseRecord, String>,
    ) = withContext(Dispatchers.IO) {
        val outputValue =
            dslContext
                .select(field)
                .from(TEST_CASE)
                .innerJoin(TEST_SUITE).on(TEST_CASE.TEST_SUITE_ID.eq(TEST_SUITE.ID))
                .innerJoin(TEST_RUN).on(TEST_SUITE.TEST_RUN_ID.eq(TEST_RUN.ID))
                .where(
                    TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id)
                        .and(TEST_SUITE.IDX.eq(testSuiteIdx))
                        .and(TEST_CASE.IDX.eq(testCaseIdx)),
                )
                .fetchOne(field)

        TestOutput(outputValue)
    }
}
