package projektor.notification.github.comment

import org.kohsuke.github.GHRepository
import org.slf4j.LoggerFactory
import projektor.notification.github.comment.GitHubCommentCreator.appendComment
import projektor.notification.github.comment.GitHubCommentCreator.createComment

class GitHubCommentService(private val commentClient: GitHubCommentClient) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    fun upsertReportComment(commentData: ReportCommentData) {
        val (orgName, repoName, branchName) = commentData.git

        val repository = commentClient.getRepository(orgName, repoName)

        repository?.let {
            val pullRequestNumber = commentClient.findOpenPullRequestsForBranch(repository, branchName)

            if (pullRequestNumber != null) {
                upsertComment(repository, pullRequestNumber, commentData)
            } else {
                logger.info("Could not find pull request for branch $branchName in repo $orgName/$repoName")
            }
        }
    }

    private fun upsertComment(repository: GHRepository, pullRequestNumber: Int, commentData: ReportCommentData) {
        val existingComment = commentClient.findCommentWithText(repository, pullRequestNumber, GitHubCommentCreator.headerText)

        if (existingComment != null) {
            val updatedCommentText = appendComment(existingComment.body, commentData)

            commentClient.updateComment(existingComment, updatedCommentText)
        } else {
            val textForNewComment = createComment(commentData)

            commentClient.addComment(repository, pullRequestNumber, textForNewComment)
        }
    }
}