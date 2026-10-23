package projektor.persistence.objectstore

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import projektor.incomingresults.mapper.normalizeFilePath
import projektor.incomingresults.mapper.parsePackageAndClassName
import projektor.objectstore.json.JsonObjectStore
import projektor.server.api.PublicId
import projektor.server.api.TestCase
import projektor.server.api.TestFailure
import projektor.server.api.TestRunSummary
import projektor.server.api.TestSuite
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import projektor.parser.model.TestSuite as ParsedTestSuite

/**
 * Reads and writes the documents that make up a test run, shared by the object-store repositories
 * that need test run, test suite or test case data.
 */
class TestRunDocumentStore(val store: JsonObjectStore) {
    private val writeConcurrency = Semaphore(MAX_CONCURRENT_WRITES)

    /**
     * The test run, or null if it doesn't exist or is being deleted.
     */
    suspend fun readTestRun(publicId: PublicId): TestRunDocument? = readTestRunIncludingDeleted(publicId)?.takeUnless { it.deleted }

    /**
     * The test run document even if the test run is being deleted, for retrying a delete that failed partway.
     */
    suspend fun readTestRunIncludingDeleted(publicId: PublicId): TestRunDocument? =
        withContext(Dispatchers.IO) {
            store.read<TestRunDocument>(ObjectStoreKeys.testRun(publicId))
        }

    /**
     * Blocking version of [readTestRun], for use inside a compare-and-swap update.
     */
    fun readTestRunBlocking(publicId: PublicId): TestRunDocument? =
        store.read<TestRunDocument>(ObjectStoreKeys.testRun(publicId))?.takeUnless { it.deleted }

    suspend fun testRunExists(publicId: PublicId): Boolean = readTestRun(publicId) != null

    /**
     * Test cases for every test suite in the run, keyed by test suite index.
     */
    suspend fun readAllTestCases(
        publicId: PublicId,
        testRun: TestRunDocument,
    ): Map<Int, List<TestCaseDocument>> =
        coroutineScope {
            testRun.testSuites
                .map { it.testCaseChunk }
                .distinct()
                .map { chunk -> async(Dispatchers.IO) { readTestCaseChunk(publicId, chunk) } }
                .awaitAll()
                .flatMap { it?.testSuites ?: listOf() }
                .associate { it.testSuiteIdx to it.testCases }
        }

    /**
     * Blocking version of [readAllTestCases], for use inside a compare-and-swap update.
     */
    fun readAllTestCasesBlocking(
        publicId: PublicId,
        testRun: TestRunDocument,
    ): Map<Int, List<TestCaseDocument>> =
        testRun.testSuites
            .map { it.testCaseChunk }
            .distinct()
            .flatMap { chunk -> readTestCaseChunk(publicId, chunk)?.testSuites.orEmpty() }
            .associate { it.testSuiteIdx to it.testCases }

    suspend fun readTestCases(
        publicId: PublicId,
        testSuite: TestSuiteDocument,
    ): List<TestCaseDocument> =
        withContext(Dispatchers.IO) {
            readTestCaseChunk(publicId, testSuite.testCaseChunk)
                ?.testSuites
                ?.find { it.testSuiteIdx == testSuite.idx }
                ?.testCases
                ?: listOf()
        }

    suspend fun readOutput(key: String): String? =
        withContext(Dispatchers.IO) {
            store.read<OutputDocument>(key)?.value
        }

    /**
     * Writes the test cases and output of the given test suites, numbering the suites from startingIdx.
     * Returns the test suite documents to add to the test run document, which callers write last
     * so a test run document never references test cases that haven't been written yet.
     */
    suspend fun writeTestSuites(
        publicId: PublicId,
        testSuites: List<ParsedTestSuite>,
        group: TestSuiteGroupDocument?,
        startingIdx: Int,
    ): List<TestSuiteDocument> {
        val documentsToWrite = mutableMapOf<String, Any>()

        val testSuiteDocuments =
            testSuites.chunked(TEST_SUITES_PER_CHUNK).flatMapIndexed { chunkIdx, chunkTestSuites ->
                val firstIdxInChunk = startingIdx + chunkIdx * TEST_SUITES_PER_CHUNK

                val chunkTestSuiteDocuments =
                    chunkTestSuites.mapIndexed { idxInChunk, testSuite ->
                        val testSuiteIdx = firstIdxInChunk + idxInChunk

                        testSuite.systemOut?.let {
                            documentsToWrite[
                                ObjectStoreKeys.testSuiteOutput(
                                    publicId,
                                    testSuiteIdx,
                                    OutputType.SYSTEM_OUT,
                                ),
                            ] = OutputDocument(it)
                        }
                        testSuite.systemErr?.let {
                            documentsToWrite[
                                ObjectStoreKeys.testSuiteOutput(
                                    publicId,
                                    testSuiteIdx,
                                    OutputType.SYSTEM_ERR,
                                ),
                            ] = OutputDocument(it)
                        }

                        val testCaseDocuments =
                            testSuite.testCases.orEmpty().mapIndexed { testCaseListIdx, testCase ->
                                val testCaseIdx = testCaseListIdx + 1

                                testCase.systemOut?.let {
                                    documentsToWrite[
                                        ObjectStoreKeys.testCaseOutput(
                                            publicId,
                                            testSuiteIdx,
                                            testCaseIdx,
                                            OutputType.SYSTEM_OUT,
                                        ),
                                    ] =
                                        OutputDocument(it)
                                }
                                testCase.systemErr?.let {
                                    documentsToWrite[
                                        ObjectStoreKeys.testCaseOutput(
                                            publicId,
                                            testSuiteIdx,
                                            testCaseIdx,
                                            OutputType.SYSTEM_ERR,
                                        ),
                                    ] =
                                        OutputDocument(it)
                                }

                                val (packageName, className) = parsePackageAndClassName(testCase.className)

                                TestCaseDocument(
                                    idx = testCaseIdx,
                                    name = testCase.name,
                                    packageName = packageName,
                                    className = className,
                                    duration = testCase.time?.toStoredScale(),
                                    passed = testCase.failure == null,
                                    skipped = testCase.skipped != null,
                                    hasSystemOut = testCase.systemOut != null,
                                    hasSystemErr = testCase.systemErr != null,
                                    failure =
                                        testCase.failure?.let { failure ->
                                            TestFailureDocument(failure.message, failure.type, failure.text)
                                        },
                                )
                            }

                        val (packageName, className) = parsePackageAndClassName(testSuite.name)

                        Pair(
                            TestSuiteDocument(
                                idx = testSuiteIdx,
                                fileName = normalizeFilePath(testSuite.file),
                                packageName = packageName,
                                className = className ?: "",
                                testCount = testSuite.tests,
                                passingCount = testSuite.passingCount,
                                skippedCount = testSuite.skipped,
                                failureCount = testSuite.failures + testSuite.errors,
                                startTs = testSuite.timestamp?.toStoredPrecision(),
                                hostname = testSuite.hostname,
                                duration = testSuite.time?.toStoredScale(),
                                hasSystemOut = !testSuite.systemOut.isNullOrBlank(),
                                hasSystemErr = !testSuite.systemErr.isNullOrBlank(),
                                groupName = group?.groupName,
                                groupLabel = group?.groupLabel,
                                testCaseChunk = firstIdxInChunk,
                            ),
                            TestSuiteTestCasesDocument(testSuiteIdx, testCaseDocuments),
                        )
                    }

                documentsToWrite[ObjectStoreKeys.testCaseChunk(publicId, firstIdxInChunk)] =
                    TestCaseChunkDocument(chunkTestSuiteDocuments.map { it.second })

                chunkTestSuiteDocuments.map { it.first }
            }

        writeAll(documentsToWrite)

        return testSuiteDocuments
    }

    suspend fun writeAll(documents: Map<String, Any>) {
        coroutineScope {
            documents
                .map { (key, document) ->
                    async(Dispatchers.IO) {
                        writeConcurrency.withPermit { store.write(key, document) }
                    }
                }
                .awaitAll()
        }
    }

    private fun readTestCaseChunk(
        publicId: PublicId,
        firstTestSuiteIdx: Int,
    ): TestCaseChunkDocument? = store.read<TestCaseChunkDocument>(ObjectStoreKeys.testCaseChunk(publicId, firstTestSuiteIdx))

    companion object {
        // Bounds how many test suites' test cases are read to fetch a single test suite or test case
        const val TEST_SUITES_PER_CHUNK = 100

        private const val MAX_CONCURRENT_WRITES = 16
    }
}

fun TestRunSummaryDocument.toApi(publicId: PublicId) =
    TestRunSummary(
        id = publicId.id,
        totalTestCount = totalTestCount,
        totalPassingCount = totalPassingCount,
        totalSkippedCount = totalSkippedCount,
        totalFailureCount = totalFailureCount,
        passed = passed,
        cumulativeDuration = cumulativeDuration,
        averageDuration = averageDuration,
        slowestTestCaseDuration = slowestTestCaseDuration,
        createdTimestamp = createdTimestamp,
        wallClockDuration = wallClockDuration,
    )

fun TestRunSummary.toDocument() =
    TestRunSummaryDocument(
        totalTestCount = totalTestCount,
        totalPassingCount = totalPassingCount,
        totalSkippedCount = totalSkippedCount,
        totalFailureCount = totalFailureCount,
        passed = passed,
        cumulativeDuration = cumulativeDuration.toStoredScale(),
        averageDuration = averageDuration.toStoredScale(),
        slowestTestCaseDuration = slowestTestCaseDuration.toStoredScale(),
        createdTimestamp = createdTimestamp.toStoredPrecision(),
        wallClockDuration = wallClockDuration?.toStoredScale(),
    )

fun TestSuiteDocument.toApi(testCases: List<TestCase>?) =
    TestSuite(
        idx = idx,
        fileName = fileName,
        packageName = packageName,
        className = className,
        testCount = testCount,
        passingCount = passingCount,
        skippedCount = skippedCount,
        failureCount = failureCount,
        startTs = startTs,
        hostname = hostname,
        duration = duration ?: BigDecimal.ZERO.toStoredScale(),
        hasSystemOut = hasSystemOut,
        hasSystemErr = hasSystemErr,
        groupName = groupName,
        groupLabel = groupLabel,
        testCases = testCases,
    )

/**
 * A test case fetched on its own (or in a list of test cases), which includes data from its test suite.
 */
fun TestCaseDocument.toApi(
    publicId: PublicId,
    testRun: TestRunDocument,
    testSuite: TestSuiteDocument,
) = TestCase(
    idx = idx,
    testSuiteIdx = testSuite.idx,
    name = name,
    packageName = testSuite.packageName,
    testSuiteName = testSuite.className,
    className = className,
    fileName = testSuite.fileName,
    duration = duration,
    passed = passed,
    skipped = skipped,
    hasSystemOutTestCase = hasSystemOut,
    hasSystemErrTestCase = hasSystemErr,
    hasSystemOutTestSuite = testSuite.hasSystemOut,
    hasSystemErrTestSuite = testSuite.hasSystemErr,
    publicId = publicId.id,
    createdTimestamp = testRun.createdLocalTimestamp(),
    failure = failure?.toApi(),
)

/**
 * A test case nested in a test suite, which only includes the test case's own data
 * (its own output flags, but not its test suite's).
 */
fun TestCaseDocument.toNestedApi(
    publicId: PublicId,
    testRun: TestRunDocument,
    testSuite: TestSuiteDocument,
) = TestCase(
    idx = idx,
    testSuiteIdx = testSuite.idx,
    name = name,
    packageName = packageName,
    testSuiteName = null,
    className = className,
    fileName = null,
    duration = duration,
    passed = passed,
    skipped = skipped,
    hasSystemOutTestCase = hasSystemOut,
    hasSystemErrTestCase = hasSystemErr,
    hasSystemOutTestSuite = false,
    hasSystemErrTestSuite = false,
    publicId = publicId.id,
    createdTimestamp = testRun.createdLocalTimestamp(),
    failure = failure?.toApi(),
)

fun TestFailureDocument.toApi() = TestFailure(failureMessage, failureType, failureText)

// The database stores the created timestamp in UTC without a time zone
fun TestRunDocument.createdLocalTimestamp(): LocalDateTime = LocalDateTime.ofInstant(summary.createdTimestamp, ZoneOffset.UTC)
