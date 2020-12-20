package projektor.notification.github

import io.ktor.util.*
import projektor.incomingresults.model.GitMetadata
import projektor.notification.NotificationConfig
import projektor.notification.github.comment.*
import projektor.server.api.TestRunSummary
import projektor.server.api.coverage.Coverage
import java.time.LocalDateTime
import java.time.ZoneId

@KtorExperimentalAPI
class GitHubPullRequestCommentService(
    private val notificationConfig: NotificationConfig,
    private val gitHubCommentService: GitHubCommentService?
) {
    fun upsertComment(testRunSummary: TestRunSummary, gitMetadata: GitMetadata?, coverage: Coverage?): PullRequest? {
        val (serverBaseUrl) = notificationConfig

        return if (gitHubCommentService != null && gitMetadata != null && serverBaseUrl != null) {
            val repoParts = gitMetadata.repoName?.split("/")
            val branchName = gitMetadata.branchName

            if (repoParts != null && repoParts.size == 2 && branchName != null) {
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

                val commentData = ReportCommentData(
                    projektorServerBaseUrl = serverBaseUrl,
                    git = ReportCommentGitData(
                        orgName = orgName,
                        repoName = repoName,
                        branchName = branchName
                    ),
                    publicId = testRunSummary.id,
                    createdDate = LocalDateTime.ofInstant(testRunSummary.createdTimestamp, ZoneId.of("Z")),
                    passed = testRunSummary.passed,
                    totalTestCount = testRunSummary.totalTestCount,
                    failedTestCount = testRunSummary.totalFailureCount,
                    coverage = coverageData,
                    project = gitMetadata.projectName
                )

                gitHubCommentService.upsertReportComment(commentData)
            } else {
                null
            }
        } else {
            null
        }
    }
}
