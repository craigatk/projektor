package projektor.testsuite

import projektor.persistence.objectstore.ObjectStoreKeys
import projektor.persistence.objectstore.OutputType
import projektor.persistence.objectstore.TestRunDocument
import projektor.persistence.objectstore.TestRunDocumentStore
import projektor.persistence.objectstore.toApi
import projektor.persistence.objectstore.toNestedApi
import projektor.server.api.PublicId
import projektor.server.api.TestOutput
import projektor.server.api.TestSuite

class TestSuiteObjectStoreRepository(private val documentStore: TestRunDocumentStore) : TestSuiteRepository {
    override suspend fun fetchTestSuite(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
    ): TestSuite? {
        val testRun = documentStore.readTestRun(testRunPublicId) ?: return null
        val testSuite = testRun.testSuites.find { it.idx == testSuiteIdx } ?: return null

        val testCases =
            documentStore.readTestCases(testRunPublicId, testSuite)
                .map { it.toNestedApi(testRunPublicId, testRun, testSuite) }

        return testSuite.toApi(testCases)
    }

    override suspend fun fetchTestSuites(
        testRunPublicId: PublicId,
        searchCriteria: TestSuiteSearchCriteria,
    ): List<TestSuite> =
        fetchTestSuitesWithCases(testRunPublicId)
            .filter { searchCriteria.packageName == null || it.packageName == searchCriteria.packageName }
            .filter { !searchCriteria.failedOnly || it.failureCount >= 1 }

    override suspend fun fetchTestSuitesWithCases(testRunPublicId: PublicId): List<TestSuite> {
        val testRun = documentStore.readTestRun(testRunPublicId) ?: return listOf()

        return fetchTestSuitesWithCases(testRunPublicId, testRun)
    }

    override suspend fun fetchTestSuiteSystemErr(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
    ): TestOutput =
        TestOutput(documentStore.readOutput(ObjectStoreKeys.testSuiteOutput(testRunPublicId, testSuiteIdx, OutputType.SYSTEM_ERR)))

    override suspend fun fetchTestSuiteSystemOut(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
    ): TestOutput =
        TestOutput(documentStore.readOutput(ObjectStoreKeys.testSuiteOutput(testRunPublicId, testSuiteIdx, OutputType.SYSTEM_OUT)))

    // Like the database repository's max() query, returns 0 when there aren't any test suites
    override suspend fun fetchHighestTestSuiteIndex(testRunPublicId: PublicId): Int? =
        documentStore.readTestRun(testRunPublicId)?.testSuites?.maxOfOrNull { it.idx } ?: 0

    private suspend fun fetchTestSuitesWithCases(
        testRunPublicId: PublicId,
        testRun: TestRunDocument,
    ): List<TestSuite> {
        val testCasesBySuite = documentStore.readAllTestCases(testRunPublicId, testRun)

        return testRun.testSuites.map { testSuite ->
            val testCases =
                testCasesBySuite[testSuite.idx].orEmpty()
                    .map { it.toNestedApi(testRunPublicId, testRun, testSuite) }

            testSuite.toApi(testCases)
        }
    }
}
