package projektor.error

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.server.api.PublicId
import projektor.server.api.error.FailureBodyType
import projektor.server.api.error.ResultsProcessingFailure
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ProcessingFailureServiceTest {
    private class CountCapturingRepository : ProcessingFailureRepository {
        var requestedCount: Int? = null

        override suspend fun recordProcessingFailure(
            publicId: PublicId,
            body: String,
            bodyType: FailureBodyType,
            failureMessage: String?,
        ): ResultsProcessingFailure = throw UnsupportedOperationException()

        override suspend fun fetchRecentProcessingFailures(failureCount: Int): List<ResultsProcessingFailure> {
            requestedCount = failureCount
            return listOf()
        }
    }

    private fun requestedCountFor(count: Int): Int? {
        val repository = CountCapturingRepository()

        runBlocking { ProcessingFailureService(repository).fetchRecentProcessingFailures(count) }

        return repository.requestedCount
    }

    @Test
    fun `should keep the requested count of recent failures within the limits`() {
        expectThat(requestedCountFor(10)).isEqualTo(10)
        expectThat(requestedCountFor(1_000_000)).isEqualTo(ProcessingFailureService.MAX_RECENT_FAILURES)
        expectThat(requestedCountFor(-1)).isEqualTo(1)
    }
}
