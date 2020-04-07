package projektor.incomingresults

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import projektor.server.api.PublicId
import projektor.server.api.results.ResultsProcessingStatus
import projektor.testrun.TestRunRepository

class GroupedTestResultsService(
    private val testResultsProcessingService: TestResultsProcessingService,
    private val groupedResultsConverter: GroupedResultsConverter,
    private val testRunRepository: TestRunRepository,
    private val metricRegistry: MeterRegistry
) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val resultsProcessSuccessCounter = metricRegistry.counter("grouped_results_process_success")
    private val resultsProcessFailureCounter = metricRegistry.counter("grouped_results_process_failure")

    suspend fun persistTestResultsAsync(groupedResultsBlob: String): PublicId {
        val publicId = randomPublicId()
        val timer = metricRegistry.timer("persist_grouped_results")
        val sample = Timer.start(metricRegistry)
        testResultsProcessingService.createResultsProcessing(publicId)

        coroutineScope.launch {
            doPersistTestResults(publicId, groupedResultsBlob)
        }

        sample.stop(timer)

        return publicId
    }

    suspend fun doPersistTestResults(publicId: PublicId, groupedResultsBlob: String) {
        try {
            testResultsProcessingService.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.PROCESSING)

            val groupedResults = groupedResultsConverter.parseAndConvertGroupedResults(groupedResultsBlob)
            testRunRepository.saveGroupedTestRun(publicId, groupedResults)

            testResultsProcessingService.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.SUCCESS)

            resultsProcessSuccessCounter.increment()
        } catch (e: Exception) {
            val errorMessage = "Error persisting test results: ${e.message}"
            handleException(publicId, groupedResultsBlob, errorMessage, e)
            resultsProcessFailureCounter.increment()
        }
    }

    private suspend fun handleException(publicId: PublicId, resultsBody: String, errorMessage: String, e: Exception) {
        logger.error(errorMessage, e)
        testResultsProcessingService.recordResultsProcessingError(publicId, resultsBody, errorMessage)
    }
}
