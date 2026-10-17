package projektor.testcase

import projektor.persistence.objectstore.ObjectStoreKeys
import projektor.persistence.objectstore.OutputType
import projektor.persistence.objectstore.TestRunDocumentStore
import projektor.persistence.objectstore.toApi
import projektor.server.api.PublicId
import projektor.server.api.TestCase
import projektor.server.api.TestOutput
import projektor.server.api.history.TestCaseHistoryEntry
import projektor.testcase.slow.TestCaseDurationBaselines

class TestCaseObjectStoreRepository(private val documentStore: TestRunDocumentStore) : TestCaseRepository {
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
    ): TestCaseDurationBaselines = TODO("Requires the repository timeline index from phase 3 of the object-store persistence plan")

    override suspend fun fetchTestCaseHistory(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
        maxRuns: Int,
    ): List<TestCaseHistoryEntry> = TODO("Requires the repository timeline index from phase 3 of the object-store persistence plan")
}
