package projektor.incomingresults.processing

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.incomingresults.randomPublicId
import projektor.parser.GroupedResultsXmlLoader
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import projektor.server.api.error.FailureBodyType
import projektor.server.api.results.ResultsProcessingStatus
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isLessThanOrEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.time.LocalDateTime

abstract class ResultsProcessingRepositoryTest : RepositoryTestCase() {
    @Test
    fun `should create new results processing record`() {
        val publicId = randomPublicId()

        val beforeCreate = LocalDateTime.now()

        val resultsProcessing = runBlocking { resultsProcessingRepository.createResultsProcessing(publicId) }

        val afterCreate = LocalDateTime.now()

        expectThat(resultsProcessing) {
            get { id }.isEqualTo(publicId.id)
            get { status }.isEqualTo(ResultsProcessingStatus.RECEIVED)
            get { createdTimestamp }.isNotNull()
                .isGreaterThanOrEqualTo(beforeCreate)
                .isLessThanOrEqualTo(afterCreate)
        }
    }

    @Test
    fun `should update results processing status`() {
        val publicId = randomPublicId()

        runBlocking { resultsProcessingRepository.createResultsProcessing(publicId) }

        runBlocking { resultsProcessingRepository.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.PROCESSING) }

        val updatedResultsProcessing = runBlocking { resultsProcessingRepository.fetchResultsProcessing(publicId) }
        expectThat(updatedResultsProcessing)
            .isNotNull()
            .get { status }.isEqualTo(ResultsProcessingStatus.PROCESSING)
    }

    @Test
    fun `should set status to error and record error message`() {
        val publicId = randomPublicId()

        runBlocking { resultsProcessingRepository.createResultsProcessing(publicId) }

        val newErrorMessage = "An error occurred"
        val resultsBody = GroupedResultsXmlLoader().passingGroupedResults()

        runBlocking { resultsProcessingRepository.recordResultsProcessingError(publicId, resultsBody, newErrorMessage) }

        val updatedResultsProcessing = runBlocking { resultsProcessingRepository.fetchResultsProcessing(publicId) }
        expectThat(updatedResultsProcessing)
            .isNotNull()
            .and {
                get { status }.isEqualTo(ResultsProcessingStatus.ERROR)
                get { errorMessage }.isEqualTo(newErrorMessage)
            }

        // The recent failures query isn't scoped to a test run and the database is shared, so look for this run's failure
        val resultsProcessingFailure =
            runBlocking {
                processingFailureRepository.fetchRecentProcessingFailures(
                    100,
                )
            }.find { it.id == publicId.id }
        expectThat(resultsProcessingFailure)
            .isNotNull()
            .and {
                get { body }.isEqualTo(resultsBody)
                get { bodyType }.isEqualTo(FailureBodyType.TEST_RESULTS)
                get { failureMessage }.isEqualTo(newErrorMessage)
            }
    }

    @Test
    fun `should fetch results processing record`() {
        val publicId = randomPublicId()

        runBlocking { resultsProcessingRepository.createResultsProcessing(publicId) }

        val resultsProcessing = runBlocking { resultsProcessingRepository.fetchResultsProcessing(publicId) }

        expectThat(resultsProcessing)
            .isNotNull()
            .and {
                get { status }.isEqualTo(ResultsProcessingStatus.RECEIVED)
            }
    }

    @Test
    fun `when trying to fetch a processing record that does not exist`() {
        val publicId = randomPublicId()

        val resultsProcessing = runBlocking { resultsProcessingRepository.fetchResultsProcessing(publicId) }

        expectThat(resultsProcessing).isNull()
    }
}

class ResultsProcessingRepositoryDatabaseTest : ResultsProcessingRepositoryTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class ResultsProcessingRepositoryObjectStoreTest : ResultsProcessingRepositoryTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
