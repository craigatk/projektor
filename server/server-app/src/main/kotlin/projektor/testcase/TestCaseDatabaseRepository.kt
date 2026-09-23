package projektor.testcase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectOnConditionStep
import org.jooq.TableField
import org.jooq.impl.DSL
import org.simpleflatmapper.jdbc.JdbcMapperFactory
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
import projektor.server.api.TestOutput
import projektor.server.api.history.TestCaseHistoryEntry
import projektor.util.addPrefixToFields
import kotlin.streams.toList

class TestCaseDatabaseRepository(private val dslContext: DSLContext) : TestCaseRepository {
    override suspend fun fetchFailedTestCases(testRunPublicId: PublicId): List<TestCase> =
        withContext(Dispatchers.IO) {
            val resultSet =
                selectTestCase(dslContext)
                    .where(
                        TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id)
                            .and(TEST_CASE.PASSED.eq(false)),
                    )
                    .orderBy(TEST_CASE.ID)
                    .fetchResultSet()

            resultSet.use { testCaseMapper.stream(it).toList() }
        }

    override suspend fun fetchSlowTestCases(
        testRunPublicId: PublicId,
        limit: Int,
    ): List<TestCase> =
        withContext(Dispatchers.IO) {
            val resultSet =
                selectTestCase(dslContext)
                    .where(TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id))
                    .orderBy(TEST_CASE.DURATION.desc())
                    .limit(limit)
                    .fetchResultSet()

            resultSet.use { testCaseMapper.stream(it).toList() }
        }

    override suspend fun fetchTestCase(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
    ): TestCase? =
        withContext(Dispatchers.IO) {
            val resultSet =
                selectTestCase(dslContext)
                    .where(
                        TEST_RUN.PUBLIC_ID.eq(testRunPublicId.id)
                            .and(TEST_SUITE.IDX.eq(testSuiteIdx))
                            .and(TEST_CASE.IDX.eq(testCaseIdx)),
                    )
                    .orderBy(TEST_CASE.ID)
                    .fetchResultSet()

            val testCase: TestCase? =
                resultSet.use {
                    testCaseMapper.stream(resultSet).findFirst().orElse(null)
                }

            testCase
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

            val repoName = target?.get(GIT_METADATA.REPO_NAME) ?: return@withContext listOf()
            val targetRunId = target.get(TEST_RUN.ID)
            val targetCreatedTimestamp = target.get(TEST_RUN.CREATED_TIMESTAMP)

            // Runs of the same repo, project, and branch up to and including the requested run.
            // Only CI runs are included, other than the requested run itself.
            val recentRunIds =
                dslContext
                    .select(TEST_RUN.ID)
                    .from(TEST_RUN)
                    .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                    .leftOuterJoin(RESULTS_METADATA).on(TEST_RUN.ID.eq(RESULTS_METADATA.TEST_RUN_ID))
                    .where(
                        GIT_METADATA.REPO_NAME.eq(repoName)
                            .and(withProjectName(target.get(GIT_METADATA.PROJECT_NAME)))
                            .and(withBranchName(target.get(GIT_METADATA.BRANCH_NAME)))
                            .and(RESULTS_METADATA.CI.isTrue.or(TEST_RUN.ID.eq(targetRunId)))
                            .and(
                                TEST_RUN.CREATED_TIMESTAMP.lt(targetCreatedTimestamp)
                                    .or(TEST_RUN.CREATED_TIMESTAMP.eq(targetCreatedTimestamp).and(TEST_RUN.ID.le(targetRunId))),
                            ),
                    )
                    .orderBy(TEST_RUN.CREATED_TIMESTAMP.desc(), TEST_RUN.ID.desc())
                    .limit(maxRuns)
                    .fetch(TEST_RUN.ID)

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

    companion object {
        val testCaseMapper =
            JdbcMapperFactory.newInstance()
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
