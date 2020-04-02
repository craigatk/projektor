package projektor.incomingresults

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import projektor.server.api.PublicId
import projektor.server.api.results.ResultsProcessingStatus
import projektor.testrun.TestRunRepository

class GroupedTestResultsService(
    private val testResultsProcessingService: TestResultsProcessingService,
    private val groupedResultsConverter: GroupedResultsConverter,
    private val testRunRepository: TestRunRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    suspend fun persistTestResultsAsync(groupedResultsXml: String): PublicId {
        val publicId = randomPublicId()
        testResultsProcessingService.createResultsProcessing(publicId)

        coroutineScope.launch {
            doPersistTestResults(publicId, groupedResultsXml)
        }

        return publicId
    }

    suspend fun doPersistTestResults(publicId: PublicId, groupedResultsXml: String) {
        try {
            testResultsProcessingService.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.PROCESSING)

            val groupedResults = groupedResultsConverter.parseAndConvertGroupedResults(groupedResultsXml)
            testRunRepository.saveGroupedTestRun(publicId, groupedResults)

            testResultsProcessingService.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.SUCCESS)
        } catch (e: Exception) {
            val errorMessage = "Error persisting test results: ${e.message}"
            handleException(publicId, errorMessage, e)
        }
    }

    private suspend fun handleException(publicId: PublicId, errorMessage: String, e: Exception) {
        logger.error(errorMessage, e)
        testResultsProcessingService.recordResultsProcessingError(publicId, errorMessage)
    }
}
