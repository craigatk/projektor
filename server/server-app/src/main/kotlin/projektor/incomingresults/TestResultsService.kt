package projektor.incomingresults

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import projektor.parser.model.TestSuite
import projektor.results.processor.TestResultsProcessor
import projektor.server.api.PublicId
import projektor.server.api.TestRunSummary
import projektor.server.api.results.ResultsProcessingStatus
import projektor.testrun.TestRunRepository

class TestResultsService(
    private val testResultsProcessor: TestResultsProcessor,
    private val testRunRepository: TestRunRepository,
    private val testResultsProcessingService: TestResultsProcessingService
) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    suspend fun persistTestResultsAsync(resultsBlob: String): PublicId {
        val publicId = randomPublicId()
        testResultsProcessingService.createResultsProcessing(publicId)

        coroutineScope.launch {
            doPersistTestResults(publicId, resultsBlob)
        }

        return publicId
    }

    suspend fun doPersistTestResults(publicId: PublicId, resultsBlob: String): TestRunSummary {
        testResultsProcessingService.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.PROCESSING)

        val testSuites = parseTestSuites(publicId, resultsBlob)
        val testRunSummary = persistTestSuites(publicId, testSuites)

        testResultsProcessingService.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.SUCCESS)

        return testRunSummary
    }

    private suspend fun parseTestSuites(publicId: PublicId, resultsBlob: String): List<TestSuite> =
        try {
            val parsedTestSuites = testResultsProcessor.parseResultsBlob(resultsBlob)
            parsedTestSuites.filter { !it.testCases.isNullOrEmpty() }
        } catch (e: Exception) {
            val errorMessage = "Error parsing test results: ${e.message}"
            handleException(publicId, errorMessage, e)
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
        testResultsProcessingService.recordResultsProcessingError(publicId, errorMessage)
        throw PersistTestResultsException(publicId, errorMessage, e)
    }
}
