package projektor.incomingresults

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import projektor.metrics.MetricsService
import projektor.parser.model.TestSuite
import projektor.results.processor.TestResultsProcessor
import projektor.server.api.PublicId
import projektor.server.api.results.ResultsProcessingStatus
import projektor.testrun.TestRunRepository

class TestResultsService(
    private val testResultsProcessor: TestResultsProcessor,
    private val testRunRepository: TestRunRepository,
    private val testResultsProcessingService: TestResultsProcessingService,
    private val metricsService: MetricsService
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

            metricsService.incrementResultsProcessSuccessCounter()
        } catch (e: JsonProcessingException) {
            val errorMessage = "Problem parsing test results: ${e.message}"
            logger.info(errorMessage, e)
            testResultsProcessingService.recordResultsProcessingError(publicId, resultsBlob, errorMessage)
            metricsService.incrementResultsParseFailureCounter()
        } catch (e: Exception) {
            val errorMessage = "Error persisting test results: ${e.message}"
            logger.error(errorMessage, e)
            testResultsProcessingService.recordResultsProcessingError(publicId, resultsBlob, errorMessage)
            metricsService.incrementResultsProcessFailureCounter()
        }
    }

    private fun parseTestSuites(resultsBlob: String): List<TestSuite> {
        val parsedTestSuites = testResultsProcessor.parseResultsBlob(resultsBlob)
        return parsedTestSuites.filter { !it.testCases.isNullOrEmpty() }
    }
}
