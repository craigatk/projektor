package projektor.testcase

import projektor.server.api.PublicId
import projektor.server.api.TestCase

class TestCaseService(private val testCaseRepository: TestCaseRepository) {

    suspend fun fetchTestCase(testRunPublicId: PublicId, testSuiteIdx: Int, testCaseIdx: Int): TestCase? =
        testCaseRepository.fetchTestCase(testRunPublicId, testSuiteIdx, testCaseIdx)

    suspend fun fetchFailedTestCases(publicId: PublicId): List<TestCase> =
        testCaseRepository.fetchFailedTestCases(publicId)

    suspend fun fetchSlowTestCases(publicId: PublicId, limit: Int): List<TestCase> =
        testCaseRepository.fetchSlowTestCases(publicId, limit)
}
