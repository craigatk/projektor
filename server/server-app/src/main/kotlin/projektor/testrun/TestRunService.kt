package projektor.testrun

import projektor.server.api.PublicId
import projektor.server.api.TestRun
import projektor.server.api.TestRunSummary

class TestRunService(private val testRunRepository: TestRunRepository) {

    suspend fun fetchTestRun(publicId: PublicId): TestRun? =
            testRunRepository.fetchTestRun(publicId)

    suspend fun fetchTestRunSummary(publicId: PublicId): TestRunSummary? =
            testRunRepository.fetchTestRunSummary(publicId)
}
