package projektor.testsuite

import projektor.server.api.PublicId
import projektor.server.api.TestOutput
import projektor.server.api.TestSuite

class TestSuiteService(private val testSuiteRepository: TestSuiteRepository) {
    suspend fun fetchTestSuite(testRunPublicId: PublicId, testSuiteIdx: Int): TestSuite? =
        testSuiteRepository.fetchTestSuite(testRunPublicId, testSuiteIdx)

    suspend fun fetchTestSuites(publicId: PublicId, testSuiteSearchCriteria: TestSuiteSearchCriteria): List<TestSuite> =
        testSuiteRepository.fetchTestSuites(publicId, testSuiteSearchCriteria)

    suspend fun fetchTestSuiteSystemErr(publicId: PublicId, testSuiteIdx: Int): TestOutput =
        testSuiteRepository.fetchTestSuiteSystemErr(publicId, testSuiteIdx)

    suspend fun fetchTestSuiteSystemOut(publicId: PublicId, testSuiteIdx: Int): TestOutput =
        testSuiteRepository.fetchTestSuiteSystemOut(publicId, testSuiteIdx)
}
