package projektor.testsuite

import projektor.server.api.PublicId
import projektor.server.api.TestSuite
import projektor.server.api.TestSuiteOutput

interface TestSuiteRepository {

    suspend fun fetchTestSuite(testRunPublicId: PublicId, testSuiteIdx: Int): TestSuite?

    suspend fun fetchTestSuites(testRunPublicId: PublicId, searchCriteria: TestSuiteSearchCriteria): List<TestSuite>

    suspend fun fetchTestSuiteSystemErr(testRunPublicId: PublicId, testSuiteIdx: Int): TestSuiteOutput

    suspend fun fetchTestSuiteSystemOut(testRunPublicId: PublicId, testSuiteIdx: Int): TestSuiteOutput
}
