package projektor.error

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.test.inject
import projektor.DatabaseRepositoryTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.error.FailureBodyType
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.time.temporal.ChronoUnit

class ProcessingFailureDatabaseRepositoryTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should fetch most recent failure`() {
        val processingFailureRepository: ProcessingFailureRepository by inject()

        val publicId = randomPublicId()
        val body = "the body"
        val bodyType = FailureBodyType.TEST_RESULTS
        val failureMessage = "the failure"

        val failureDB =
            runBlocking {
                processingFailureRepository.recordProcessingFailure(
                    publicId,
                    body,
                    bodyType,
                    failureMessage,
                )
            }

        // fetchRecentProcessingFailures has no per-test scoping (it's a global "most recent
        // across the server" query) and this table isn't isolated between concurrently-running
        // tests, so fetch a generous window and find our own record in it rather than assuming
        // it's the single most recent row -- another test's concurrent insert could otherwise
        // make this flaky.
        val failures = runBlocking { processingFailureRepository.fetchRecentProcessingFailures(1000) }

        val failure = failures.find { it.id == publicId.id }

        expectThat(failure)
            .isNotNull()
            .and {
                get { body }.isEqualTo(body)
                get { bodyType }.isEqualTo(bodyType)
                get { failureMessage }.isEqualTo(failureMessage)
                get { createdTimestamp.truncatedTo(ChronoUnit.MILLIS) }.isEqualTo(failureDB.createdTimestamp.truncatedTo(ChronoUnit.MILLIS))
            }
    }
}
