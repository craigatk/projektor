package projektor.incomingresults.processing

import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.get
import projektor.DatabaseRepositoryTestCase
import projektor.database.generated.tables.pojos.ResultsProcessing
import projektor.incomingresults.randomPublicId
import projektor.parser.GroupedResultsXmlLoader
import projektor.server.api.results.ResultsProcessingStatus
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

class ResultsProcessingDatabaseRepositoryTest : DatabaseRepositoryTestCase() {
    private lateinit var resultsProcessingDatabaseRepository: ResultsProcessingRepository

    @BeforeEach
    fun createRepository() {
        resultsProcessingDatabaseRepository = get()
    }

    @Test
    fun `should create new results processing record`() {
        val publicId = randomPublicId()

        val resultsProcessing = runBlocking { resultsProcessingDatabaseRepository.createResultsProcessing(publicId) }

        val now = LocalDateTime.now()

        expectThat(resultsProcessing) {
            get { id }.isEqualTo(publicId.id)
            get { status }.isEqualTo(ResultsProcessingStatus.RECEIVED)
            get { createdTimestamp }.isNotNull()
                    .and {
                        get { year }.isEqualTo(now.year)
                        get { month }.isEqualTo(now.month)
                        get { dayOfMonth }.isEqualTo(now.dayOfMonth)
                        get { hour }.isEqualTo(now.hour)
                        get { minute }.isEqualTo(now.minute)
                    }
        }
    }

    @Test
    fun `should update results processing status`() {
        val publicId = randomPublicId()

        val resultsProcessingDB = ResultsProcessing()
                .setPublicId(publicId.id)
                .setStatus(ResultsProcessingStatus.RECEIVED.name)
        resultsProcessingDao.insert(resultsProcessingDB)

        runBlocking { resultsProcessingDatabaseRepository.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.PROCESSING) }

        val updatedResultsProcessing = resultsProcessingDao.fetchOneByPublicId(publicId.id)
        expectThat(updatedResultsProcessing)
                .isNotNull()
                .get { status }.isEqualTo(ResultsProcessingStatus.PROCESSING.name)
    }

    @Test
    fun `should set status to error and record error message`() {
        val publicId = randomPublicId()

        val resultsProcessingDB = ResultsProcessing()
                .setPublicId(publicId.id)
                .setStatus(ResultsProcessingStatus.RECEIVED.name)
        resultsProcessingDao.insert(resultsProcessingDB)

        val newErrorMessage = "An error occurred"
        val resultsBody = GroupedResultsXmlLoader().passingGroupedResults()

        runBlocking { resultsProcessingDatabaseRepository.recordResultsProcessingError(publicId, resultsBody, newErrorMessage) }

        val updatedResultsProcessing = resultsProcessingDao.fetchOneByPublicId(publicId.id)
        expectThat(updatedResultsProcessing)
                .isNotNull()
                .and {
                    get { status }.isEqualTo(ResultsProcessingStatus.ERROR.name)
                    get { errorMessage }.isEqualTo(newErrorMessage)
                }

        val resultsProcessingFailure = resultsProcessingFailureDao.fetchOneByPublicId(publicId.id)
        expectThat(resultsProcessingFailure)
                .isNotNull()
                .and {
                    get { resultsBody }.isEqualTo(resultsBody)
                }
    }

    @Test
    fun `should fetch results processing record`() {
        val publicId = randomPublicId()

        val resultsProcessingDB = ResultsProcessing()
                .setPublicId(publicId.id)
                .setStatus(ResultsProcessingStatus.RECEIVED.name)
        resultsProcessingDao.insert(resultsProcessingDB)

        val resultsProcessing = runBlocking { resultsProcessingDatabaseRepository.fetchResultsProcessing(publicId) }

        expectThat(resultsProcessing)
                .isNotNull()
                .and {
                    get { status }.isEqualTo(ResultsProcessingStatus.RECEIVED)
                }
    }

    @Test
    fun `when trying to fetch a processing record that does not exist`() {
        val publicId = randomPublicId()

        val resultsProcessing = runBlocking { resultsProcessingDatabaseRepository.fetchResultsProcessing(publicId) }

        expectThat(resultsProcessing).isNull()
    }
}
