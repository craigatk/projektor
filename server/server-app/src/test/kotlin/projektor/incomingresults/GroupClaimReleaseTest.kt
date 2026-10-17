package projektor.incomingresults

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.awaitility.kotlin.untilNotNull
import org.junit.jupiter.api.Test
import projektor.compare.PreviousTestRunService
import projektor.incomingresults.model.GroupedResults
import projektor.metrics.MetricsService
import projektor.notification.NotificationConfig
import projektor.notification.github.GitHubPullRequestCommentService
import projektor.notification.slack.RepositorySlackChannelService
import projektor.notification.slack.SlackMainlineNotificationService
import projektor.notification.slack.SlackNotificationConfig
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.GroupedResultsParser
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import projektor.parser.performance.PerformanceResultsParser
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import projektor.repository.testrun.RepositoryTestRunService
import projektor.results.processor.TestResultsProcessor
import projektor.server.api.PublicId
import projektor.server.api.TestRunSummary
import projektor.server.api.results.ResultsProcessingStatus
import projektor.testrun.TestRunRepository
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEqualTo
import strikt.assertions.isNull
import java.util.concurrent.TimeUnit

/**
 * When the first upload with a results group fails to save its test run, its claim on the group is released
 * so the next upload with the group starts a new test run rather than appending to one that doesn't exist.
 */
abstract class GroupClaimReleaseTest : RepositoryTestCase() {
    @Test
    fun `when first upload with group fails to save should release group for next upload`() {
        val failingTestRunRepository = FailFirstSaveTestRunRepository(testRunRepository)
        val groupedTestResultsService = createGroupedTestResultsService(failingTestRunRepository)
        val resultsBody = groupedResultsBody(repoName = "org-${RandomStringUtils.randomAlphabetic(10).lowercase()}/repo", group = "B1")

        val failedPublicId = runBlocking { groupedTestResultsService.persistTestResultsAsync(resultsBody) }
        awaitProcessingStatus(failedPublicId, ResultsProcessingStatus.ERROR)
        expectThat(runBlocking { testRunRepository.fetchTestRunSummary(failedPublicId) }).isNull()

        val nextPublicId = runBlocking { groupedTestResultsService.persistTestResultsAsync(resultsBody) }

        expectThat(nextPublicId).isNotEqualTo(failedPublicId)
        awaitProcessingStatus(nextPublicId, ResultsProcessingStatus.SUCCESS)
        await.atMost(30, TimeUnit.SECONDS) untilNotNull { runBlocking { testRunRepository.fetchTestRunSummary(nextPublicId) } }

        // Later uploads with the group append to the new test run
        val appendedPublicId = runBlocking { groupedTestResultsService.persistTestResultsAsync(resultsBody) }
        expectThat(appendedPublicId).isEqualTo(nextPublicId)
    }

    @Test
    fun `when test run saved but a later step fails should keep group claim`() {
        val repoName = "org-${RandomStringUtils.randomAlphabetic(10).lowercase()}/repo"
        val publicId = randomPublicId()

        runBlocking {
            testRunRepository.claimTestRunGroup("B1", repoName, publicId)
            testRunGenerator.createSimpleTestRun(publicId)
        }

        // The claim release only applies when the test run wasn't saved
        expectThat(runBlocking { testRunRepository.claimTestRunGroup("B1", repoName, randomPublicId()) }).isEqualTo(publicId)

        runBlocking { testRunRepository.releaseTestRunGroupClaim("B1", repoName, randomPublicId()) }
        expectThat(runBlocking { testRunRepository.claimTestRunGroup("B1", repoName, randomPublicId()) }).isEqualTo(publicId)

        runBlocking { testRunRepository.releaseTestRunGroupClaim("B1", repoName, publicId) }
        val newPublicId = randomPublicId()
        expectThat(runBlocking { testRunRepository.claimTestRunGroup("B1", repoName, newPublicId) }).isEqualTo(newPublicId)
    }

    private fun awaitProcessingStatus(
        publicId: PublicId,
        status: ResultsProcessingStatus,
    ) {
        await.atMost(
            30,
            TimeUnit.SECONDS,
        ) until { runBlocking { resultsProcessingRepository.fetchResultsProcessing(publicId)?.status } == status }
    }

    private fun createGroupedTestResultsService(testRunRepository: TestRunRepository): GroupedTestResultsService {
        val notificationConfig = NotificationConfig(null)
        val previousTestRunService = PreviousTestRunService(previousTestRunRepository)

        return GroupedTestResultsService(
            TestResultsProcessingService(resultsProcessingRepository),
            GroupedResultsConverter(
                GroupedResultsParser(GroupedResultsParser.DEFAULT_MAX_PAYLOAD_SIZE),
                PerformanceResultsParser(),
                TestResultsProcessor(),
            ),
            testRunRepository,
            testRunMetadataRepository,
            performanceResultsRepository,
            MetricsService(SimpleMeterRegistry()),
            GitHubPullRequestCommentService(notificationConfig, null),
            coverageService,
            AppendTestResultsService(testRunRepository, testSuiteRepository, resultsProcessingRepository),
            codeQualityReportRepository,
            SlackMainlineNotificationService(
                notificationConfig,
                SlackNotificationConfig(null, null),
                null,
                RepositorySlackChannelService(repositorySlackChannelRepository, null),
                previousTestRunService,
                RepositoryTestRunService(repositoryTestRunRepository),
            ),
        )
    }

    private fun groupedResultsBody(
        repoName: String,
        group: String,
    ): String {
        val gitMetadata = GitMetadata()
        gitMetadata.repoName = repoName
        gitMetadata.branchName = "main"
        gitMetadata.isMainBranch = true
        val metadata = ResultsMetadata()
        metadata.git = gitMetadata
        metadata.ci = true
        metadata.group = group

        return GroupedResultsXmlLoader().passingGroupedResults(metadata)
    }

    private class FailFirstSaveTestRunRepository(private val delegate: TestRunRepository) : TestRunRepository by delegate {
        @Volatile
        private var failNextSave = true

        override suspend fun saveGroupedTestRun(
            publicId: PublicId,
            groupedResults: GroupedResults,
        ): TestRunSummary {
            if (failNextSave) {
                failNextSave = false
                throw IllegalStateException("Simulated failure saving test run $publicId")
            }

            return delegate.saveGroupedTestRun(publicId, groupedResults)
        }
    }
}

class GroupClaimReleaseDatabaseTest : GroupClaimReleaseTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class GroupClaimReleaseObjectStoreTest : GroupClaimReleaseTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
