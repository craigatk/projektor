package projektor.notification.slack

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import projektor.compare.PreviousTestRunService
import projektor.incomingresults.model.GitMetadata
import projektor.notification.NotificationConfig
import projektor.notification.slack.message.CoverageDropData
import projektor.notification.slack.message.FlakyTestData
import projektor.notification.slack.message.MainlineFailureData
import projektor.notification.slack.message.NewFlakyTestsData
import projektor.notification.slack.message.SlackNotificationData
import projektor.repository.testrun.RepositoryTestRunService
import projektor.server.api.PublicId
import projektor.server.api.TestRunSummary
import projektor.server.api.coverage.Coverage
import projektor.server.api.repository.BranchType

class SlackMainlineNotificationService(
    private val notificationConfig: NotificationConfig,
    private val slackNotificationConfig: SlackNotificationConfig,
    private val slackNotificationService: SlackNotificationService?,
    private val repositorySlackChannelService: RepositorySlackChannelService,
    private val previousTestRunService: PreviousTestRunService,
    private val repositoryTestRunService: RepositoryTestRunService,
) {
    /**
     * Sends a Slack notification to the channel mapped to the run's repo and project when a CI run on
     * the mainline branch goes red, drops coverage, or makes a test flaky.
     *
     * @return the channel the notification was sent to, or null if no notification was needed
     */
    suspend fun notifyIfNeeded(
        testRunSummary: TestRunSummary,
        gitMetadata: GitMetadata?,
        ci: Boolean?,
        coverage: Coverage?,
    ): String? {
        val serverBaseUrl = notificationConfig.serverBaseUrl
        val repoName = gitMetadata?.repoName

        if (slackNotificationService == null || serverBaseUrl == null || repoName == null || !gitMetadata.isMainBranch || ci != true) {
            return null
        }

        val projectName = gitMetadata.projectName
        val channelName = repositorySlackChannelService.fetchChannelName(repoName, projectName) ?: return null

        val mainlineFailure = findMainlineFailure(testRunSummary)
        val coverageDrop = findCoverageDrop(coverage)
        val newFlakyTests = findNewFlakyTests(testRunSummary, repoName, projectName)

        if (mainlineFailure == null && coverageDrop == null && newFlakyTests == null) {
            return null
        }

        val notificationData =
            SlackNotificationData(
                projektorServerBaseUrl = serverBaseUrl,
                publicId = testRunSummary.id,
                repoName = repoName,
                projectName = projectName,
                branchName = gitMetadata.branchName,
                commitSha = gitMetadata.commitSha,
                mainlineFailure = mainlineFailure,
                coverageDrop = coverageDrop,
                newFlakyTests = newFlakyTests,
            )

        withContext(Dispatchers.IO) {
            slackNotificationService.sendNotification(channelName, notificationData)
        }

        return channelName
    }

    // Only notify when mainline changes from passing to failing, not on every failing run
    private suspend fun findMainlineFailure(testRunSummary: TestRunSummary): MainlineFailureData? {
        if (testRunSummary.passed) {
            return null
        }

        val previousRun = previousTestRunService.findPreviousMainBranchRun(PublicId(testRunSummary.id))

        return if (previousRun == null || previousRun.passed) {
            MainlineFailureData(
                failedTestCount = testRunSummary.totalFailureCount,
                totalTestCount = testRunSummary.totalTestCount,
            )
        } else {
            null
        }
    }

    private fun findCoverageDrop(coverage: Coverage?): CoverageDropData? {
        val lineStat = coverage?.overallStats?.lineStat
        val coverageDelta = lineStat?.coveredPercentageDelta

        return if (lineStat != null && coverageDelta != null && coverageDelta <= slackNotificationConfig.coverageDropThreshold.negate()) {
            CoverageDropData(
                lineCoveredPercentage = lineStat.coveredPercentage,
                lineCoverageDelta = coverageDelta,
            )
        } else {
            null
        }
    }

    // A test is newly flaky when its failure in this run is the one that got it to the flaky threshold
    private suspend fun findNewFlakyTests(
        testRunSummary: TestRunSummary,
        repoName: String,
        projectName: String?,
    ): NewFlakyTestsData? {
        if (testRunSummary.totalFailureCount == 0) {
            return null
        }

        val flakyTests =
            repositoryTestRunService.fetchFlakyTests(
                repoName = repoName,
                projectName = projectName,
                maxRuns = slackNotificationConfig.flakyMaxRuns,
                flakyFailureThreshold = slackNotificationConfig.flakyFailureThreshold,
                branchType = BranchType.MAINLINE,
            )

        val newFlakyTests =
            flakyTests
                .filter { it.latestTestCase.publicId == testRunSummary.id && it.failureCount == slackNotificationConfig.flakyFailureThreshold }
                .map {
                    FlakyTestData(
                        name = it.testCase.fullName,
                        failureCount = it.failureCount,
                        failurePercentage = it.failurePercentage,
                    )
                }

        return if (newFlakyTests.isNotEmpty()) NewFlakyTestsData(newFlakyTests) else null
    }
}
