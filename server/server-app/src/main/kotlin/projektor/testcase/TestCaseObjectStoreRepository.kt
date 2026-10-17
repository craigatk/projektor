package projektor.testcase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import projektor.persistence.objectstore.ObjectStoreKeys
import projektor.persistence.objectstore.OutputType
import projektor.persistence.objectstore.TestRunDocument
import projektor.persistence.objectstore.TestRunDocumentStore
import projektor.persistence.objectstore.TestRunIndex
import projektor.persistence.objectstore.TestRunIndexEntry
import projektor.persistence.objectstore.toApi
import projektor.server.api.PublicId
import projektor.server.api.TestCase
import projektor.server.api.TestOutput
import projektor.server.api.history.TestCaseHistoryEntry
import projektor.testcase.slow.TestCaseDurationBaseline
import projektor.testcase.slow.TestCaseDurationBaselines
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.ceil
import kotlin.math.floor

class TestCaseObjectStoreRepository(
    private val documentStore: TestRunDocumentStore,
    private val testRunIndex: TestRunIndex,
) : TestCaseRepository {
    override suspend fun fetchFailedTestCases(testRunPublicId: PublicId): List<TestCase> =
        fetchTestCases(testRunPublicId).filter { !it.passed }

    override suspend fun fetchSlowTestCases(
        testRunPublicId: PublicId,
        limit: Int,
    ): List<TestCase> =
        fetchTestCases(testRunPublicId)
            // Like Postgres' descending order, test cases without a duration sort first
            .sortedWith(compareByDescending<TestCase> { it.duration == null }.thenByDescending { it.duration })
            .take(limit)

    override suspend fun fetchTestCases(testRunPublicId: PublicId): List<TestCase> {
        val testRun = documentStore.readTestRun(testRunPublicId) ?: return listOf()
        val testCasesBySuite = documentStore.readAllTestCases(testRunPublicId, testRun)

        return testRun.testSuites.flatMap { testSuite ->
            testCasesBySuite[testSuite.idx].orEmpty().map { it.toApi(testRunPublicId, testRun, testSuite) }
        }
    }

    override suspend fun fetchTestCase(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
    ): TestCase? {
        val testRun = documentStore.readTestRun(testRunPublicId) ?: return null
        val testSuite = testRun.testSuites.find { it.idx == testSuiteIdx } ?: return null

        return documentStore.readTestCases(testRunPublicId, testSuite)
            .find { it.idx == testCaseIdx }
            ?.toApi(testRunPublicId, testRun, testSuite)
    }

    override suspend fun fetchTestCaseSystemErr(
        publicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
    ): TestOutput =
        TestOutput(documentStore.readOutput(ObjectStoreKeys.testCaseOutput(publicId, testSuiteIdx, testCaseIdx, OutputType.SYSTEM_ERR)))

    override suspend fun fetchTestCaseSystemOut(
        publicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
    ): TestOutput =
        TestOutput(documentStore.readOutput(ObjectStoreKeys.testCaseOutput(publicId, testSuiteIdx, testCaseIdx, OutputType.SYSTEM_OUT)))

    override suspend fun fetchTestCaseDurationBaselines(
        testRunPublicId: PublicId,
        maxRuns: Int,
    ): TestCaseDurationBaselines {
        val noBaselines = TestCaseDurationBaselines(0, listOf())

        val targetTestRun = documentStore.readTestRun(testRunPublicId) ?: return noBaselines
        val baselineRuns = fetchRecentRuns(testRunPublicId, targetTestRun, maxRuns, includeTargetRun = false) ?: return noBaselines

        if (baselineRuns.isEmpty()) return noBaselines

        val baselineSamples =
            coroutineScope {
                baselineRuns
                    .map { entry -> async(Dispatchers.IO) { Pair(entry.publicId, readAllTestCases(PublicId(entry.publicId))) } }
                    .awaitAll()
            }.flatMap { (publicId, testCases) ->
                testCases
                    .filter { it.passed && !it.skipped && it.duration != null }
                    .map { BaselineSample(TestCaseKey(it.packageName, it.className, it.name), publicId, it.duration!!) }
            }.groupBy { it.testCaseKey }

        val targetTestCasesBySuite = documentStore.readAllTestCases(testRunPublicId, targetTestRun)

        val baselines =
            targetTestRun.testSuites.flatMap { testSuite ->
                targetTestCasesBySuite[testSuite.idx].orEmpty().mapNotNull { testCase ->
                    baselineSamples[TestCaseKey(testCase.packageName, testCase.className, testCase.name)]?.let { samples ->
                        TestCaseDurationBaseline(
                            testSuiteIdx = testSuite.idx,
                            testCaseIdx = testCase.idx,
                            medianDuration = postgresMedian(samples.map { it.duration }),
                            sampleCount = samples.map { it.publicId }.distinct().size,
                        )
                    }
                }
            }

        return TestCaseDurationBaselines(baselineRuns.size, baselines)
    }

    override suspend fun fetchTestCaseHistory(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
        maxRuns: Int,
    ): List<TestCaseHistoryEntry> {
        val targetTestRun = documentStore.readTestRun(testRunPublicId) ?: return listOf()
        val targetTestSuite = targetTestRun.testSuites.find { it.idx == testSuiteIdx } ?: return listOf()
        val targetTestCase = documentStore.readTestCases(testRunPublicId, targetTestSuite).find { it.idx == testCaseIdx } ?: return listOf()

        // Like the database's class_name = ? condition, a test case without a class name never matches
        val targetClassName = targetTestCase.className ?: return listOf()

        val recentRuns = fetchRecentRuns(testRunPublicId, targetTestRun, maxRuns, includeTargetRun = true) ?: return listOf()

        val testCasesByRun =
            coroutineScope {
                recentRuns
                    .map { entry -> async(Dispatchers.IO) { readAllTestCases(PublicId(entry.publicId)) } }
                    .awaitAll()
            }

        return recentRuns.zip(testCasesByRun).mapNotNull { (entry, testCases) ->
            testCases
                .filter { it.name == targetTestCase.name && it.className == targetClassName && it.packageName == targetTestCase.packageName }
                // If a run has the same test multiple times, prefer the one that was requested
                .sortedWith(
                    compareBy<IndexedTestCase>({
                        if (it.testSuiteIdx == testSuiteIdx && it.idx == testCaseIdx) 0 else 1
                    }, { it.testSuiteIdx }, { it.idx }),
                )
                .firstOrNull()
                ?.let { testCase ->
                    TestCaseHistoryEntry(
                        publicId = entry.publicId,
                        testSuiteIdx = testCase.testSuiteIdx,
                        testCaseIdx = testCase.idx,
                        createdTimestamp = LocalDateTime.ofInstant(entry.createdTimestamp, ZoneOffset.UTC),
                        passed = testCase.passed,
                        skipped = testCase.skipped,
                        duration = testCase.duration,
                        branchName = entry.branchName,
                        commitSha = entry.commitSha,
                        pullRequestNumber = entry.pullRequestNumber,
                    )
                }
        }
    }

    /**
     * Most recent CI runs of the same repo, project, and branch as the target run, ordered most recent first.
     * The target run itself is included (even if it wasn't a CI run) only when [includeTargetRun] is true.
     * Returns null when the target run doesn't have Git metadata.
     */
    private suspend fun fetchRecentRuns(
        targetPublicId: PublicId,
        targetTestRun: TestRunDocument,
        maxRuns: Int,
        includeTargetRun: Boolean,
    ): List<TestRunIndexEntry>? {
        val targetGit = targetTestRun.gitMetadata ?: return null

        val entries = testRunIndex.readRepositoryEntries(targetGit.repoName)
        val targetPosition = entries.indexOfFirst { it.publicId == targetPublicId.id }

        if (targetPosition < 0) return null

        // Entries are in created order, with runs created at the same time in the order they were saved
        val candidates = entries.subList(0, if (includeTargetRun) targetPosition + 1 else targetPosition)

        return candidates
            .filter { entry ->
                entry.matchesProject(targetGit.projectName) &&
                    entry.matchesBranchName(targetGit.branchName) &&
                    (entry.isCI || entry.publicId == targetPublicId.id)
            }
            .asReversed()
            .take(maxRuns)
    }

    private suspend fun readAllTestCases(publicId: PublicId): List<IndexedTestCase> {
        val testRun = documentStore.readTestRun(publicId) ?: return listOf()
        val testCasesBySuite = documentStore.readAllTestCases(publicId, testRun)

        return testRun.testSuites.flatMap { testSuite ->
            testCasesBySuite[testSuite.idx].orEmpty().map { testCase ->
                IndexedTestCase(
                    testSuiteIdx = testSuite.idx,
                    idx = testCase.idx,
                    name = testCase.name,
                    packageName = testCase.packageName,
                    className = testCase.className,
                    passed = testCase.passed,
                    skipped = testCase.skipped,
                    duration = testCase.duration,
                )
            }
        }
    }

    private data class IndexedTestCase(
        val testSuiteIdx: Int,
        val idx: Int,
        val name: String,
        val packageName: String?,
        val className: String?,
        val passed: Boolean,
        val skipped: Boolean,
        val duration: BigDecimal?,
    )

    private data class TestCaseKey(val packageName: String?, val className: String?, val name: String)

    private data class BaselineSample(val testCaseKey: TestCaseKey, val publicId: String, val duration: BigDecimal)

    companion object {
        /**
         * The median as Postgres' percentile_cont(0.5) calculates it: in double precision, interpolating
         * between the two middle values. Converted to a BigDecimal like the database repository's results once the
         * Postgres driver switches the query to binary transfer (3.0 rather than 3); before that switch the database
         * returns the same value at a different scale.
         */
        internal fun postgresMedian(values: List<BigDecimal>): BigDecimal {
            val sorted = values.map { it.toDouble() }.sorted()
            val position = (sorted.size - 1) * 0.5
            val lower = sorted[floor(position).toInt()]
            val upper = sorted[ceil(position).toInt()]
            val median = lower + (upper - lower) * (position - floor(position))

            return BigDecimal.valueOf(median)
        }
    }
}
