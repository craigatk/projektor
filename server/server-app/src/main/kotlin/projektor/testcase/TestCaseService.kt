package projektor.testcase

import projektor.server.api.PublicId
import projektor.server.api.TestCase
import projektor.server.api.TestOutput

class TestCaseService(private val testCaseRepository: TestCaseRepository) {

    suspend fun fetchTestCase(testRunPublicId: PublicId, testSuiteIdx: Int, testCaseIdx: Int): TestCase? =
        testCaseRepository.fetchTestCase(testRunPublicId, testSuiteIdx, testCaseIdx)

    suspend fun fetchFailedTestCases(publicId: PublicId): List<TestCase> =
        testCaseRepository.fetchFailedTestCases(publicId)

    suspend fun fetchSlowTestCases(publicId: PublicId, limit: Int): List<TestCase> =
        testCaseRepository.fetchSlowTestCases(publicId, limit)

    suspend fun fetchTestCaseSystemErr(publicId: PublicId, testSuiteIdx: Int, testCaseIdx: Int): TestOutput =
        testCaseRepository.fetchTestCaseSystemErr(publicId, testSuiteIdx, testCaseIdx)

    suspend fun fetchTestCaseSystemOut(publicId: PublicId, testSuiteIdx: Int, testCaseIdx: Int): TestOutput =
        testCaseRepository.fetchTestCaseSystemOut(publicId, testSuiteIdx, testCaseIdx)
}
