package projektor.incomingresults

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.runBlocking
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilNotNull
import org.koin.core.get
import projektor.DatabaseRepositoryTestCase
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.parser.ResultsXmlLoader
import projektor.server.api.results.ResultsProcessingStatus
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.*

class TestResultsServiceTest : DatabaseRepositoryTestCase() {
    private lateinit var testResultsService: TestResultsService

    @BeforeTest
    fun setUpService() {
        testResultsService = get()
    }

    @Test
    fun `when processing succeeds should record processing results`() {
        val resultsBlob = ResultsXmlLoader().passing()

        val publicId = runBlocking { testResultsService.persistTestResultsAsync(resultsBlob) }

        await untilNotNull { testRunDao.fetchOneByPublicId(publicId.id) }

        val resultsProcessing = resultsProcessingDao.fetchOneByPublicId(publicId.id)
        expectThat(resultsProcessing)
                .isNotNull()
                .and {
                    get { status }.isEqualTo(ResultsProcessingStatus.SUCCESS.name)
                }
    }

    @Test
    fun `when processing fails should record processing results with error`() {
        val publicId = randomPublicId()
        val invalidXml = "<testsuites>Mismatched closing tag</testsuite>"
        val resultsProcessingRepository: ResultsProcessingRepository = get()

        runBlocking { resultsProcessingRepository.createResultsProcessing(publicId) }

        expectCatching { runBlocking { testResultsService.doPersistTestResults(publicId, invalidXml) } }
                .failed()
                .isA<PersistTestResultsException>()

        val resultsProcessing = resultsProcessingDao.fetchOneByPublicId(publicId.id)

        expectThat(resultsProcessing)
                .isNotNull()
                .get { status }.isEqualTo(ResultsProcessingStatus.ERROR.name)
    }
}
