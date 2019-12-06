package projektor.testcase

import projektor.server.api.PublicId
import projektor.server.api.TestCase

interface TestCaseRepository {

    suspend fun fetchFailedTestCases(testRunPublicId: PublicId): List<TestCase>

    suspend fun fetchSlowTestCases(testRunPublicId: PublicId, limit: Int): List<TestCase>

    suspend fun fetchTestCase(testRunPublicId: PublicId, testSuiteIdx: Int, testCaseIdx: Int): TestCase?
}
