package projektor.notification.github

import io.ktor.util.*
import projektor.incomingresults.model.GitMetadata
import projektor.notification.NotificationConfig
import projektor.notification.github.comment.GitHubCommentService
import projektor.notification.github.comment.PullRequest
import projektor.notification.github.comment.ReportCommentData
import projektor.notification.github.comment.ReportCommentGitData
import projektor.server.api.TestRunSummary
import java.time.LocalDateTime
import java.time.ZoneId

@KtorExperimentalAPI
class GitHubPullRequestCommentService(
    private val notificationConfig: NotificationConfig,
    private val gitHubCommentService: GitHubCommentService?
) {
    fun upsertComment(testRunSummary: TestRunSummary, gitMetadata: GitMetadata?): PullRequest? {
        val (serverBaseUrl) = notificationConfig

        return if (gitHubCommentService != null && gitMetadata != null && serverBaseUrl != null) {
            val repoParts = gitMetadata.repoName?.split("/")
            val branchName = gitMetadata.branchName

            if (repoParts != null && repoParts.size == 2 && branchName != null) {
                val orgName = repoParts[0]
                val repoName = repoParts[1]

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
                    coverage = null,
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
