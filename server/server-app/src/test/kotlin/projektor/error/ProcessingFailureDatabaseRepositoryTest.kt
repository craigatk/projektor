package projektor.error

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.test.inject
import projektor.DatabaseRepositoryTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.error.FailureBodyType
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.time.temporal.ChronoUnit

class ProcessingFailureDatabaseRepositoryTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should fetch most recent failure`() {
        val processingFailureRepository: ProcessingFailureRepository by inject()

        val publicId = randomPublicId()
        val body = "the body"
        val bodyType = FailureBodyType.TEST_RESULTS
        val failureMessage = "the failure"

        val failureDB = runBlocking {
            processingFailureRepository.recordProcessingFailure(
                publicId,
                body,
                bodyType,
                failureMessage
            )
        }

        val failures = runBlocking { processingFailureRepository.fetchRecentProcessingFailures(1) }
        expectThat(failures).hasSize(1)

        val failure = failures[0]
        expectThat(failure) {
            get { id }.isEqualTo(publicId.id)
            get { body }.isEqualTo(body)
            get { bodyType }.isEqualTo(bodyType)
            get { failureMessage }.isEqualTo(failureMessage)
            get { createdTimestamp.truncatedTo(ChronoUnit.MILLIS) }.isEqualTo(failureDB.createdTimestamp.truncatedTo(ChronoUnit.MILLIS))
        }
    }
}
