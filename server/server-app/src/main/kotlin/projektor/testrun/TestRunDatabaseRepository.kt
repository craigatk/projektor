package projektor.testrun

import io.opentelemetry.api.GlobalOpenTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.Configuration
import org.jooq.DSLContext
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import org.slf4j.LoggerFactory
import projektor.database.generated.Tables.GIT_METADATA
import projektor.database.generated.Tables.GIT_REPOSITORY
import projektor.database.generated.Tables.RESULTS_METADATA
import projektor.database.generated.Tables.TEST_RUN
import projektor.database.generated.Tables.TEST_RUN_ATTACHMENT
import projektor.database.generated.Tables.TEST_RUN_SYSTEM_ATTRIBUTES
import projektor.database.generated.Tables.TEST_SUITE
import projektor.database.generated.Tables.TEST_SUITE_GROUP
import projektor.database.generated.tables.daos.GitMetadataDao
import projektor.database.generated.tables.daos.ResultsMetadataDao
import projektor.database.generated.tables.daos.TestCaseDao
import projektor.database.generated.tables.daos.TestFailureDao
import projektor.database.generated.tables.daos.TestRunDao
import projektor.database.generated.tables.daos.TestSuiteDao
import projektor.database.generated.tables.daos.TestSuiteGroupDao
import projektor.incomingresults.mapper.toDB
import projektor.incomingresults.mapper.toTestRunSummary
import projektor.incomingresults.mapper.toTestRunSummaryFromApi
import projektor.incomingresults.model.GitMetadata
import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.incomingresults.model.ResultsMetadata
import projektor.server.api.PublicId
import projektor.server.api.TestRun
import projektor.server.api.TestRunSummary
import projektor.server.api.TestSuite
import projektor.telemetry.startSpanWithParent
import projektor.util.addPrefixToFields
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import projektor.database.generated.tables.pojos.TestSuiteGroup as TestSuiteGroupDB
import projektor.parser.model.TestSuite as ParsedTestSuite

class TestRunDatabaseRepository(private val dslContext: DSLContext) : TestRunRepository {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    private val testRunMapper = JdbcMapperFactory.newInstance()
        .addKeys("id", "test_suites_id")
        .ignorePropertyNotFound()
        .newMapper(TestRun::class.java)

    private val tracer = GlobalOpenTelemetry.getTracer("projektor.TestRunDatabaseRepository")

    override suspend fun saveTestRun(publicId: PublicId, testSuites: List<ParsedTestSuite>) =
        withContext(Dispatchers.IO) {
            val testRunSummary = toTestRunSummary(publicId, testSuites, null)
            val testRunDB = testRunSummary.toDB()

            dslContext.transaction { configuration ->
                val testRunDao = TestRunDao(configuration)

                testRunDao.insert(testRunDB)

                saveTestSuites(testSuites, testRunDB.id, null, 0, configuration)

                logger.info("Finished inserting test run $publicId")
            }

            testRunSummary
        }

    override suspend fun saveGroupedTestRun(publicId: PublicId, groupedResults: GroupedResults) =
        withContext(Dispatchers.IO) {
            val testSuites = groupedResults.groupedTestSuites.flatMap { it.testSuites }
            val testRunSummary = toTestRunSummary(publicId, testSuites, groupedResults.wallClockDuration)
            val testRunDB = testRunSummary.toDB()

            logger.info("Starting inserting test run $publicId")

            dslContext.transaction { configuration ->
                val testRunDao = TestRunDao(configuration)
                val testSuiteGroupDao = TestSuiteGroupDao(configuration)

                testRunDao.insert(testRunDB)

                var testSuiteStartingIndex = 0

                groupedResults.groupedTestSuites.forEach { groupedTestSuites ->
                    val testSuiteGroupDB = groupedTestSuites.toDB(testRunDB.id)
                    testSuiteGroupDao.insert(testSuiteGroupDB)

                    saveTestSuites(groupedTestSuites.testSuites, testRunDB.id, testSuiteGroupDB.id, testSuiteStartingIndex, configuration)

                    testSuiteStartingIndex += groupedTestSuites.testSuites.size
                }

                saveResultsMetadata(testRunDB.id, groupedResults.metadata, configuration)
                saveGitMetadata(testRunDB.id, groupedResults.metadata?.git, configuration)
            }

            saveGitRepository(groupedResults.metadata?.git)

            logger.info("Finished inserting test run $publicId")

            Pair(testRunDB.id, testRunSummary)
        }

    override suspend fun appendTestSuites(publicId: PublicId, startingIdx: Int, groupedTestSuites: List<GroupedTestSuites>): Long =
        withContext(Dispatchers.IO) {
            var testRunId: Long = 0
            logger.info("Starting appending test suites to $publicId")

            var testSuiteStartingIndex = startingIdx

            dslContext.transaction { configuration ->
                val testRunDao = TestRunDao(configuration)
                val testSuiteGroupDao = TestSuiteGroupDao(configuration)

                val testRunDB = testRunDao.fetchOneByPublicId(publicId.id)

                groupedTestSuites.forEach { groupedTestSuites ->
                    val existingTestSuiteDB = fetchTestSuiteGroup(testRunDB.id, groupedTestSuites.groupName)

                    val testSuiteGroupDB = if (existingTestSuiteDB != null) {
                        existingTestSuiteDB
                    } else {
                        val newTestSuiteGroupDB = groupedTestSuites.toDB(testRunDB.id)
                        testSuiteGroupDao.insert(newTestSuiteGroupDB)
                        newTestSuiteGroupDB
                    }

                    saveTestSuites(groupedTestSuites.testSuites, testRunDB.id, testSuiteGroupDB.id, testSuiteStartingIndex, configuration)

                    testSuiteStartingIndex += groupedTestSuites.testSuites.size
                }

                testRunId = testRunDB.id
            }

            testRunId
        }

    override suspend fun updateTestRunSummary(testRunId: Long, testSuites: List<TestSuite>, wallClockDuration: BigDecimal?): TestRunSummary =
        withContext(Dispatchers.IO) {
            val testRunDao = TestRunDao(dslContext.configuration())
            val existingTestRun = testRunDao.fetchOneById(testRunId)

            val updatedWallClockDuration: BigDecimal = (existingTestRun.wallClockDuration ?: BigDecimal.ZERO) + (wallClockDuration ?: BigDecimal.ZERO)

            val testRunSummary = toTestRunSummaryFromApi(PublicId(existingTestRun.publicId), testSuites, updatedWallClockDuration)
            existingTestRun.averageDuration = testRunSummary.averageDuration
            existingTestRun.cumulativeDuration = testRunSummary.cumulativeDuration
            existingTestRun.passed = testRunSummary.passed
            existingTestRun.slowestTestCaseDuration = testRunSummary.slowestTestCaseDuration
            existingTestRun.totalFailureCount = testRunSummary.totalFailureCount
            existingTestRun.totalPassingCount = testRunSummary.totalPassingCount
            existingTestRun.totalSkippedCount = testRunSummary.totalSkippedCount
            existingTestRun.totalTestCount = testRunSummary.totalTestCount
            existingTestRun.wallClockDuration = testRunSummary.wallClockDuration

            testRunDao.update(existingTestRun)

            testRunSummary
        }

    private fun saveTestSuites(
        testSuites: List<ParsedTestSuite>,
        testRunId: Long,
        testGroupId: Long?,
        testSuiteStartingIndex: Int,
        configuration: Configuration
    ) {
        val testSuiteDao = TestSuiteDao(configuration)
        val testCaseDao = TestCaseDao(configuration)
        val testFailureDao = TestFailureDao(configuration)

        testSuites.forEachIndexed { testSuiteIdx, testSuite ->
            val testSuiteDB = testSuite.toDB(testRunId, testGroupId, testSuiteStartingIndex + testSuiteIdx + 1)
            testSuiteDao.insert(testSuiteDB)

            testSuite.testCases.forEachIndexed { testCaseIdx, testCase ->
                val testCaseDB = testCase.toDB(testSuiteDB.id, testCaseIdx + 1)
                testCaseDao.insert(testCaseDB)

                if (testCase.failure != null) {
                    val testFailureDB = testCase.failure.toDB(testCaseDB.id)
                    testFailureDao.insert(testFailureDB)
                }
            }
        }
    }

    private fun saveResultsMetadata(
        testRunId: Long,
        resultsMetadata: ResultsMetadata?,
        configuration: Configuration
    ) {
        val resultsMetadataDao = ResultsMetadataDao(configuration)

        val resultsMetadataDB = resultsMetadata?.toDB(testRunId)

        resultsMetadataDB?.let { resultsMetadataDao.insert(it) }
    }

    private fun saveGitMetadata(
        testRunId: Long,
        git: GitMetadata?,
        configuration: Configuration
    ) {
        val gitMetadataDao = GitMetadataDao(configuration)

        val gitMetadataDB = git?.toDB(testRunId)

        if (gitMetadataDB?.repoName != null) {
            gitMetadataDao.insert(gitMetadataDB)
        }
    }

    private fun saveGitRepository(gitMetadata: GitMetadata?) {
        try {
            gitMetadata?.let { git ->
                val repoName = git.repoName

                if (repoName != null) {
                    val orgName = repoName.split("/").first()

                    dslContext.insertInto(GIT_REPOSITORY, GIT_REPOSITORY.REPO_NAME, GIT_REPOSITORY.ORG_NAME)
                        .values(repoName, orgName)
                        .onDuplicateKeyUpdate()
                        .set(GIT_REPOSITORY.ORG_NAME, orgName)
                        .execute()
                }
            }
        } catch (e: Exception) {
            logger.error("Error saving Git Repository data $gitMetadata", e)
        }
    }

    private fun fetchTestSuiteGroup(testRunId: Long, groupName: String?): TestSuiteGroupDB? =
        dslContext
            .select(TEST_SUITE_GROUP.fields().toList())
            .from(TEST_SUITE_GROUP)
            .where(TEST_SUITE_GROUP.TEST_RUN_ID.eq(testRunId).and(TEST_SUITE_GROUP.GROUP_NAME.eq(groupName)))
            .fetchOneInto(TestSuiteGroupDB::class.java)

    override suspend fun fetchTestRun(publicId: PublicId): TestRun? =
        withContext(Dispatchers.IO) {
            val span = tracer.startSpanWithParent("projektor.fetchTestRun")

            val resultSet = dslContext
                .select(TEST_RUN.PUBLIC_ID.`as`("id"))
                .select(TEST_RUN.addPrefixToFields("summary"))
                .select(TEST_SUITE.addPrefixToFields("test_suites_"))
                .select(TEST_SUITE_GROUP.GROUP_LABEL.`as`("test_suites_group_label"))
                .select(TEST_SUITE_GROUP.GROUP_NAME.`as`("test_suites_group_name"))
                .from(TEST_RUN)
                .leftOuterJoin(TEST_SUITE).on(TEST_SUITE.TEST_RUN_ID.eq(TEST_RUN.ID))
                .leftOuterJoin(TEST_SUITE_GROUP).on(TEST_SUITE.TEST_SUITE_GROUP_ID.eq(TEST_SUITE_GROUP.ID))
                .where(TEST_RUN.PUBLIC_ID.eq(publicId.id))
                .orderBy(TEST_RUN.ID)
                .fetchResultSet()

            val mapperSpan = tracer.startSpanWithParent("projektor.fetchTestRun.mapper")

            val testRun: TestRun? = resultSet.use {
                testRunMapper.stream(resultSet).findFirst().orElse(null)
            }

            mapperSpan.end()

            span.end()

            testRun
        }

    override suspend fun fetchTestRunSummary(publicId: PublicId): TestRunSummary? =
        withContext(Dispatchers.IO) {
            val span = tracer.startSpanWithParent("projektor.fetchTestRunSummary")

            val testRunSummary = dslContext
                .select(TEST_RUN.PUBLIC_ID.`as`("id"))
                .select(TEST_RUN.fields().filterNot { it.name == "id" }.toList())
                .from(TEST_RUN)
                .where(TEST_RUN.PUBLIC_ID.eq(publicId.id))
                .fetchOneInto(TestRunSummary::class.java)

            span.end()

            testRunSummary
        }

    override suspend fun deleteTestRun(publicId: PublicId) {
        withContext(Dispatchers.IO) {
            dslContext.transaction { configuration ->
                val testRunDao = TestRunDao(configuration)

                val testRun = testRunDao.fetchOneByPublicId(publicId.id)

                testRun?.let {
                    testRunDao.deleteById(testRun.id)
                }
            }
        }
    }

    override suspend fun findTestRunsCreatedBeforeAndNotPinned(createdBefore: LocalDate): List<PublicId> =
        findTestRunsCreatedBeforeAndNotPinned(createdBefore, false)

    override suspend fun findTestRunsCreatedBeforeAndNotPinnedWithAttachments(createdBefore: LocalDate): List<PublicId> =
        findTestRunsCreatedBeforeAndNotPinned(createdBefore, true)

    override suspend fun findTestRunWithGroup(group: String, repoName: String): PublicId? =
        withContext(Dispatchers.IO) {
            dslContext
                .select(TEST_RUN.PUBLIC_ID)
                .from(TEST_RUN)
                .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                .innerJoin(RESULTS_METADATA).on(TEST_RUN.ID.eq(RESULTS_METADATA.TEST_RUN_ID))
                .where(GIT_METADATA.REPO_NAME.eq(repoName).and(RESULTS_METADATA.GROUP.eq(group)))
                .limit(1)
                .fetchOneInto(String::class.java)
                ?.let { PublicId(it) }
        }

    private suspend fun findTestRunsCreatedBeforeAndNotPinned(createdBefore: LocalDate, withAttachmentsOnly: Boolean): List<PublicId> =
        withContext(Dispatchers.IO) {
            val createdBeforeTimestamp = LocalDateTime.of(createdBefore, LocalTime.MIDNIGHT)

            val query = if (withAttachmentsOnly) {
                dslContext
                    .select(TEST_RUN.PUBLIC_ID)
                    .from(TEST_RUN)
                    .leftOuterJoin(TEST_RUN_SYSTEM_ATTRIBUTES).on(TEST_RUN_SYSTEM_ATTRIBUTES.TEST_RUN_PUBLIC_ID.eq(TEST_RUN.PUBLIC_ID))
                    .innerJoin(TEST_RUN_ATTACHMENT).on(TEST_RUN.PUBLIC_ID.eq(TEST_RUN_ATTACHMENT.TEST_RUN_PUBLIC_ID))
            } else {
                dslContext
                    .select(TEST_RUN.PUBLIC_ID)
                    .from(TEST_RUN)
                    .leftOuterJoin(TEST_RUN_SYSTEM_ATTRIBUTES).on(TEST_RUN_SYSTEM_ATTRIBUTES.TEST_RUN_PUBLIC_ID.eq(TEST_RUN.PUBLIC_ID))
            }

            query.where(
                TEST_RUN_SYSTEM_ATTRIBUTES.PINNED.isNull
                    .or(TEST_RUN_SYSTEM_ATTRIBUTES.PINNED.isFalse)
                    .and(TEST_RUN.CREATED_TIMESTAMP.lessOrEqual(createdBeforeTimestamp))
            )
                .fetchInto(String::class.java)
                .map { PublicId(it) }
        }
}
