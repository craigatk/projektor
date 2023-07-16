package projektor.notification.github

import projektor.incomingresults.model.GitMetadata
import projektor.notification.NotificationConfig
import projektor.notification.github.comment.GitHubCommentService
import projektor.notification.github.comment.PullRequest
import projektor.notification.github.comment.ReportCommentData
import projektor.notification.github.comment.ReportCommentGitData
import projektor.notification.github.comment.ReportCommentPerformanceData
import projektor.notification.github.comment.ReportCoverageCommentData
import projektor.server.api.TestRunSummary
import projektor.server.api.coverage.Coverage
import projektor.server.api.performance.PerformanceResult
import java.time.LocalDateTime
import java.time.ZoneId

class GitHubPullRequestCommentService(
    private val notificationConfig: NotificationConfig,
    private val gitHubCommentService: GitHubCommentService?
) {
    fun upsertComment(
        testRunSummary: TestRunSummary,
        gitMetadata: GitMetadata?,
        coverage: Coverage?,
        performanceResults: List<PerformanceResult>?
    ): PullRequest? {
        val (serverBaseUrl) = notificationConfig

        return if (gitHubCommentService != null && gitMetadata != null && serverBaseUrl != null) {
            val repoParts = gitMetadata.repoName?.split("/")
            val branchName = gitMetadata.branchName
            val commitSha = gitMetadata.commitSha
            val pullRequestNumber = gitMetadata.pullRequestNumber

            if (repoParts != null && repoParts.size == 2) {
                val orgName = repoParts[0]
                val repoName = repoParts[1]

                val coverageData = if (coverage != null) {
                    ReportCoverageCommentData(
                        lineCoveredPercentage = coverage.overallStats.lineStat.coveredPercentage,
                        lineCoverageDelta = coverage.overallStats.lineStat.coveredPercentageDelta
                    )
                } else {
                    null
                }

                val performanceData = performanceResults?.map { performanceResult ->
                    ReportCommentPerformanceData(
                        name = performanceResult.name,
                        requestsPerSecond = performanceResult.requestsPerSecond,
                        p95 = performanceResult.p95
                    )
                }

                val commentData = ReportCommentData(
                    projektorServerBaseUrl = serverBaseUrl,
                    git = ReportCommentGitData(
                        orgName = orgName,
                        repoName = repoName,
                        branchName = branchName,
                        commitSha = commitSha,
                        pullRequestNumber = pullRequestNumber
                    ),
                    publicId = testRunSummary.id,
                    createdDate = LocalDateTime.ofInstant(testRunSummary.createdTimestamp, ZoneId.of("Z")),
                    passed = testRunSummary.passed,
                    totalTestCount = testRunSummary.totalTestCount,
                    failedTestCount = testRunSummary.totalFailureCount,
                    performance = performanceData,
                    coverage = coverageData,
                    project = gitMetadata.projectName
                )

                gitHubCommentService.upsertReportComment(commentData, testRunSummary.id)
            } else {
                null
            }
        } else {
            null
        }
    }
}
