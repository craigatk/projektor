package projektor.testcase

import projektor.server.api.PublicId
import projektor.server.api.TestCase
import projektor.server.api.TestOutput
import projektor.server.api.history.TestCaseHistoryEntry

interface TestCaseRepository {
    suspend fun fetchFailedTestCases(testRunPublicId: PublicId): List<TestCase>

    suspend fun fetchSlowTestCases(
        testRunPublicId: PublicId,
        limit: Int,
    ): List<TestCase>

    suspend fun fetchTestCase(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
    ): TestCase?

    suspend fun fetchTestCaseSystemErr(
        publicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
    ): TestOutput

    suspend fun fetchTestCaseSystemOut(
        publicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
    ): TestOutput

    suspend fun fetchTestCaseHistory(
        testRunPublicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
        maxRuns: Int,
    ): List<TestCaseHistoryEntry>
}
