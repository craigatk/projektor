package projektor.testrun

import io.opentelemetry.api.trace.Span
import projektor.server.api.PublicId
import projektor.server.api.TestRun
import projektor.server.api.TestRunSummary

class TestRunService(private val testRunRepository: TestRunRepository) {

    suspend fun fetchTestRun(publicId: PublicId): TestRun? =
        testRunRepository.fetchTestRun(publicId)

    suspend fun fetchTestRunSummary(publicId: PublicId): TestRunSummary? =
        testRunRepository.fetchTestRunSummary(publicId)

    fun addTestRunSummaryToSpan(testRunSummary: TestRunSummary) {
        val span = Span.current()
        span?.setAttribute("projektor.id", testRunSummary.id)
        span?.setAttribute("projektor.total_passing_count", testRunSummary.totalPassingCount.toLong())
        span?.setAttribute("projektor.total_failure_count", testRunSummary.totalFailureCount.toLong())
        span?.setAttribute("projektor.total_test_count", testRunSummary.totalTestCount.toLong())
    }
}
