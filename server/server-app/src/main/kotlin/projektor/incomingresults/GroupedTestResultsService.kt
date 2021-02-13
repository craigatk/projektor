package projektor.incomingresults

import com.fasterxml.jackson.core.JsonProcessingException
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import projektor.coverage.CoverageService
import projektor.incomingresults.model.GitMetadata
import projektor.incomingresults.model.GroupedResults
import projektor.metrics.MetricsService
import projektor.notification.github.GitHubPullRequestCommentService
import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.performance.PerformanceResultsRepository
import projektor.server.api.PublicId
import projektor.server.api.TestRunSummary
import projektor.server.api.coverage.Coverage
import projektor.server.api.results.ResultsProcessingStatus
import projektor.testrun.TestRunRepository

@KtorExperimentalAPI
class GroupedTestResultsService(
    private val testResultsProcessingService: TestResultsProcessingService,
    private val groupedResultsConverter: GroupedResultsConverter,
    private val testRunRepository: TestRunRepository,
    private val performanceResultsRepository: PerformanceResultsRepository,
    private val metricsService: MetricsService,
    private val gitHubPullRequestCommentService: GitHubPullRequestCommentService,
    private val coverageService: CoverageService
) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    suspend fun persistTestResultsAsync(groupedResultsBlob: String): PublicId {
        val publicId = randomPublicId()
        val timer = metricsService.createTimer("persist_grouped_results")
        testResultsProcessingService.createResultsProcessing(publicId)

        val groupedResults = try {
            groupedResultsConverter.parseAndConvertGroupedResults(groupedResultsBlob)
        } catch (e: JsonProcessingException) {
            val errorMessage = "Error parsing test results: ${e.message}"
            logger.info(errorMessage, e)
            metricsService.incrementResultsParseFailureCounter()
            testResultsProcessingService.recordResultsProcessingError(publicId, groupedResultsBlob, errorMessage)
            throw PersistTestResultsException(publicId, errorMessage, e)
        } catch (e: Exception) {
            val errorMessage = "Error persisting test results: ${e.message}"
            logger.error(errorMessage, e)
            metricsService.incrementResultsProcessFailureCounter()
            testResultsProcessingService.recordResultsProcessingError(publicId, groupedResultsBlob, errorMessage)
            throw PersistTestResultsException(publicId, errorMessage, e)
        }

        coroutineScope.launch {
            doPersistTestResults(publicId, groupedResults, groupedResultsBlob)
        }

        metricsService.stopTimer(timer)

        return publicId
    }

    suspend fun doPersistTestResults(publicId: PublicId, groupedResults: GroupedResults, groupedResultsBlob: String) {
        try {
            val (testRunId, testRunSummary) = testRunRepository.saveGroupedTestRun(publicId, groupedResults)

            groupedResults.performanceResults.forEach { performanceResult ->
                performanceResultsRepository.savePerformanceResults(testRunId, publicId, performanceResult)
            }

            testResultsProcessingService.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.SUCCESS)

            val coverage = groupedResults.coverageFiles?.let { saveCoverage(publicId, it) }

            metricsService.incrementResultsProcessSuccessCounter()

            publishCommentToPullRequest(testRunSummary, groupedResults.metadata?.git, coverage)
        } catch (e: Exception) {
            val errorMessage = "Error persisting test results: ${e.message}"
            logger.error(errorMessage, e)
            testResultsProcessingService.recordResultsProcessingError(publicId, groupedResultsBlob, errorMessage)
            metricsService.incrementResultsProcessFailureCounter()
        }
    }

    private suspend fun saveCoverage(publicId: PublicId, coverageFiles: List<CoverageFilePayload>): Coverage? {
        coverageFiles.forEach { coverageFile ->
            try {
                coverageService.saveReport(coverageFile, publicId)
            } catch (e: Exception) {
                // Error is logged inside coverageService.saveReport
            }
        }

        return coverageService.getCoverageWithPreviousRunComparison(publicId)
    }

    private fun publishCommentToPullRequest(testRunSummary: TestRunSummary, gitMetadata: GitMetadata?, coverage: Coverage?) {
        try {
            val pullRequest = gitHubPullRequestCommentService.upsertComment(testRunSummary, gitMetadata, coverage)

            if (pullRequest != null) {
                logger.info("Successfully commented on pull request ${pullRequest.number} in ${pullRequest.orgName}/${pullRequest.repoName}")

                metricsService.incrementPullRequestCommentSuccessCounter()
            }
        } catch (e: Exception) {
            logger.warn("Error publishing comment to pull request for test run ${testRunSummary.id}", e)
            metricsService.incrementPullRequestCommentFailureCounter()
        }
    }
}
