package projektor.incomingresults

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import projektor.incomingresults.model.GroupedResults
import projektor.server.api.PublicId
import projektor.server.api.TestRunSummary
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

    suspend fun doPersistTestResults(publicId: PublicId, groupedResultsXml: String): TestRunSummary {
        testResultsProcessingService.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.PROCESSING)

        val groupedResults = parseGroupedResults(publicId, groupedResultsXml)
        val testRunSummary = persistGroupedResults(publicId, groupedResults)

        testResultsProcessingService.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.SUCCESS)

        return testRunSummary
    }

    private suspend fun parseGroupedResults(publicId: PublicId, groupedResultsXml: String): GroupedResults =
        try {
            groupedResultsConverter.parseAndConvertGroupedResults(groupedResultsXml)
        } catch (e: Exception) {
            val errorMessage = "Error parsing test results: ${e.message}"
            handleException(publicId, errorMessage, e)
        }

    private suspend fun persistGroupedResults(publicId: PublicId, groupedResults: GroupedResults): TestRunSummary =
            try {
                testRunRepository.saveGroupedTestRun(publicId, groupedResults)
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
