package projektor.testsuite

import projektor.server.api.PublicId
import projektor.server.api.TestOutput
import projektor.server.api.TestSuite

interface TestSuiteRepository {
    suspend fun fetchTestSuite(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
    ): TestSuite?

    suspend fun fetchTestSuites(
        testRunPublicId: PublicId,
        searchCriteria: TestSuiteSearchCriteria,
    ): List<TestSuite>

    suspend fun fetchTestSuitesWithCases(testRunPublicId: PublicId): List<TestSuite>

    suspend fun fetchTestSuiteSystemErr(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
    ): TestOutput

    suspend fun fetchTestSuiteSystemOut(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
    ): TestOutput

    suspend fun fetchHighestTestSuiteIndex(testRunPublicId: PublicId): Int?
}
