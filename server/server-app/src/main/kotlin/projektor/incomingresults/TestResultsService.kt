package projektor.incomingresults

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import projektor.parser.model.TestSuite
import projektor.results.processor.TestResultsProcessor
import projektor.server.api.PublicId
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

    suspend fun doPersistTestResults(publicId: PublicId, resultsBlob: String) {
        try {
            testResultsProcessingService.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.PROCESSING)

            val testSuites = parseTestSuites(resultsBlob)
            testRunRepository.saveTestRun(publicId, testSuites)

            testResultsProcessingService.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.SUCCESS)
        } catch (e: Exception) {
            val errorMessage = "Error persisting test results: ${e.message}"
            handleException(publicId, errorMessage, e)
        }
    }

    private fun parseTestSuites(resultsBlob: String): List<TestSuite> {
        val parsedTestSuites = testResultsProcessor.parseResultsBlob(resultsBlob)
        return parsedTestSuites.filter { !it.testCases.isNullOrEmpty() }
    }

    private suspend fun handleException(publicId: PublicId, errorMessage: String, e: Exception) {
        logger.error(errorMessage, e)
        testResultsProcessingService.recordResultsProcessingError(publicId, errorMessage)
    }
}
