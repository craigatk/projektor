package projektor.incomingresults

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.parser.JUnitResultsParser
import projektor.parser.model.TestSuite
import projektor.server.api.PublicId
import projektor.server.api.ResultsProcessing
import projektor.server.api.ResultsProcessingStatus
import projektor.server.api.TestRunSummary
import projektor.testrun.TestRunRepository

@kotlinx.coroutines.ObsoleteCoroutinesApi
class TestResultsService(
    private val JUnitResultsParser: JUnitResultsParser,
    private val testRunRepository: TestRunRepository,
    private val resultsProcessingRepository: ResultsProcessingRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    private val asyncSave = newFixedThreadPoolContext(8, "saveTestResults")

    suspend fun persistTestResultsAsync(resultsBlob: String): PublicId {
        val publicId = randomPublicId()
        resultsProcessingRepository.createResultsProcessing(publicId)

        GlobalScope.launch(asyncSave, CoroutineStart.DEFAULT) {
            doPersistTestResults(publicId, resultsBlob)
        }

        return publicId
    }

    suspend fun fetchResultsProcessing(publicId: PublicId): ResultsProcessing? =
            resultsProcessingRepository.fetchResultsProcessing(publicId)

    suspend fun doPersistTestResults(publicId: PublicId, resultsBlob: String): TestRunSummary {
        resultsProcessingRepository.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.PROCESSING)

        val testSuites = parseTestSuites(publicId, resultsBlob)
        val testRunSummary = persistTestSuites(publicId, testSuites)

        resultsProcessingRepository.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.SUCCESS)

        return testRunSummary
    }

    private suspend fun parseTestSuites(publicId: PublicId, resultsBlob: String): List<TestSuite> {
        val testSuitesWithTestCases = try {
            val parsedTestSuites = JUnitResultsParser.parseResultsBlob(resultsBlob)
            parsedTestSuites.filter { !it.testCases.isNullOrEmpty() }
        } catch (e: Exception) {
            val errorMessage = "Error parsing test results: ${e.message}"
            handleException(publicId, errorMessage, e)
        }

        return testSuitesWithTestCases
    }

    private suspend fun persistTestSuites(publicId: PublicId, testSuites: List<TestSuite>): TestRunSummary =
        try {
            testRunRepository.saveTestRun(publicId, testSuites)
        } catch (e: Exception) {
            val errorMessage = "Error saving test results: ${e.message}"
            handleException(publicId, errorMessage, e)
        }

    private suspend fun handleException(publicId: PublicId, errorMessage: String, e: Exception): Nothing {
        logger.error(errorMessage, e)
        resultsProcessingRepository.recordResultsProcessingError(publicId, errorMessage)
        throw PersistTestResultsException(publicId, errorMessage, e)
    }
}
