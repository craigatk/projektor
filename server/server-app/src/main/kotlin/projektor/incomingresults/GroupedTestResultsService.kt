package projektor.incomingresults

import com.fasterxml.jackson.core.JsonProcessingException
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
import projektor.server.api.performance.PerformanceResult
import projektor.server.api.results.ResultsProcessingStatus
import projektor.testrun.TestRunRepository

class GroupedTestResultsService(
    private val testResultsProcessingService: TestResultsProcessingService,
    private val groupedResultsConverter: GroupedResultsConverter,
    private val testRunRepository: TestRunRepository,
    private val performanceResultsRepository: PerformanceResultsRepository,
    private val metricsService: MetricsService,
    private val gitHubPullRequestCommentService: GitHubPullRequestCommentService,
    private val coverageService: CoverageService,
    private val appendTestResultsService: AppendTestResultsService
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
            val errorMessage = "Problem parsing test results: ${e.message}"
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

        val group = groupedResults.metadata?.group
        val repoAndOrgName = groupedResults.metadata?.git?.repoName
        val existingPublicIdWithGroup = appendTestResultsService.findExistingTestRunWithGroup(group, repoAndOrgName)

        coroutineScope.launch {
            if (existingPublicIdWithGroup != null) {
                doAppendTestResults(existingPublicIdWithGroup, groupedResults, groupedResultsBlob)
            } else {
                doPersistTestResults(publicId, groupedResults, groupedResultsBlob)
            }
        }

        metricsService.stopTimer(timer)

        return existingPublicIdWithGroup ?: publicId
    }

    suspend fun doPersistTestResults(publicId: PublicId, groupedResults: GroupedResults, groupedResultsBlob: String) {
        try {
            val (testRunId, testRunSummary) = testRunRepository.saveGroupedTestRun(publicId, groupedResults)

            val performanceResults = groupedResults.performanceResults.map { performanceResult ->
                performanceResultsRepository.savePerformanceResults(testRunId, publicId, performanceResult)
            }

            testResultsProcessingService.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.SUCCESS)

            val coverage = groupedResults.coverageFiles?.let { saveCoverage(publicId, it) }

            metricsService.incrementResultsProcessSuccessCounter()

            publishCommentToPullRequest(testRunSummary, groupedResults.metadata?.git, coverage, performanceResults)
        } catch (e: Exception) {
            val errorMessage = "Error persisting test results: ${e.message}"
            logger.error(errorMessage, e)
            testResultsProcessingService.recordResultsProcessingError(publicId, groupedResultsBlob, errorMessage)
            metricsService.incrementResultsProcessFailureCounter()
        }
    }

    suspend fun doAppendTestResults(publicId: PublicId, groupedResults: GroupedResults, groupedResultsBlob: String) {
        try {
            val (_, testRunSummary) = appendTestResultsService.appendGroupedTestRun(publicId, groupedResults)

            metricsService.incrementResultsProcessSuccessCounter()

            val coverage = groupedResults.coverageFiles?.let {
                coverageService.appendCoverage(publicId, it)

                coverageService.getCoverage(publicId)
            }

            publishCommentToPullRequest(testRunSummary, groupedResults.metadata?.git, coverage, null)
        } catch (e: Exception) {
            val errorMessage = "Error appending test results: ${e.message}"
            logger.error(errorMessage, e)
            testResultsProcessingService.recordResultsProcessingError(publicId, groupedResultsBlob, errorMessage)
            metricsService.incrementResultsProcessFailureCounter()
        }
    }

    private suspend fun saveCoverage(publicId: PublicId, coverageFiles: List<CoverageFilePayload>): Coverage? {
        coverageService.appendCoverage(publicId, coverageFiles)

        return coverageService.getCoverageWithPreviousRunComparison(publicId)
    }

    private fun publishCommentToPullRequest(
        testRunSummary: TestRunSummary,
        gitMetadata: GitMetadata?,
        coverage: Coverage?,
        performanceResults: List<PerformanceResult>?
    ) {
        try {
            val pullRequest = gitHubPullRequestCommentService.upsertComment(testRunSummary, gitMetadata, coverage, performanceResults)

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
