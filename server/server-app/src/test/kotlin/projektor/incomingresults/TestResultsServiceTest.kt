package projektor.incomingresults

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.untilNotNull
import org.junit.jupiter.api.Test
import org.koin.test.inject
import projektor.DatabaseRepositoryTestCase
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.parser.ResultsXmlLoader
import projektor.server.api.results.ResultsProcessingStatus
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@KtorExperimentalAPI
class TestResultsServiceTest : DatabaseRepositoryTestCase() {
    private val testResultsService by inject<TestResultsService>()

    @Test
    fun `when processing succeeds should record processing results`() {
        val resultsBlob = ResultsXmlLoader().passing()

        val publicId = runBlocking { testResultsService.persistTestResultsAsync(resultsBlob) }

        await untilNotNull { testRunDao.fetchOneByPublicId(publicId.id) }

        await untilAsserted {
            val resultsProcessing = resultsProcessingDao.fetchOneByPublicId(publicId.id)
            expectThat(resultsProcessing)
                .isNotNull()
                .and {
                    get { status }.isEqualTo(ResultsProcessingStatus.SUCCESS.name)
                }
        }
    }

    @Test
    fun `when processing fails should record processing results with error`() {
        val publicId = randomPublicId()
        val invalidXml = "<testsuites>Mismatched closing tag</testsuite>"
        val resultsProcessingRepository by inject<ResultsProcessingRepository>()

        runBlocking { resultsProcessingRepository.createResultsProcessing(publicId) }

        runBlocking { testResultsService.doPersistTestResults(publicId, invalidXml) }

        await untilAsserted {
            val resultsProcessing = resultsProcessingDao.fetchOneByPublicId(publicId.id)

            expectThat(resultsProcessing)
                .isNotNull()
                .get { status }.isEqualTo(ResultsProcessingStatus.ERROR.name)
        }
    }
}
