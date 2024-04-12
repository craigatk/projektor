package projektor.notification.github.comment

import org.kohsuke.github.GHRepository
import org.slf4j.LoggerFactory
import projektor.notification.github.comment.GitHubCommentCreator.appendComment
import projektor.notification.github.comment.GitHubCommentCreator.createComment

class GitHubCommentService(private val commentClient: GitHubCommentClient) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    fun upsertReportComment(
        commentData: ReportCommentData,
        publicId: String,
    ): PullRequest? {
        val (orgName, repoName, branchName, commitSha, pullRequestNumber) = commentData.git

        val repository = commentClient.getRepository(orgName, repoName)

        return if (repository != null) {
            logger.info(
                "Searching for pull request with pull request number $pullRequestNumber branch name $branchName or Git commit SHA $commitSha in repo $orgName/$repoName for public ID $publicId",
            )

            val prNumber =
                if (pullRequestNumber != null) {
                    pullRequestNumber
                } else if (branchName != null || commitSha != null) {
                    commentClient.findOpenPullRequests(repository, branchName, commitSha)
                } else {
                    logger.info(
                        "Need branch name or pull request number to find pull request in repo $orgName/$repoName for public ID $publicId",
                    )
                    null
                }

            if (prNumber != null) {
                upsertComment(repository, prNumber, commentData)
                PullRequest(
                    orgName = orgName,
                    repoName = repoName,
                    number = prNumber,
                )
            } else {
                logger.info(
                    "Could not find pull request for branch $branchName or commit SHA $commitSha in repo $orgName/$repoName for public ID $publicId",
                )
                null
            }
        } else {
            null
        }
    }

    private fun upsertComment(
        repository: GHRepository,
        pullRequestNumber: Int,
        commentData: ReportCommentData,
    ) {
        val existingComment = commentClient.findCommentWithText(repository, pullRequestNumber, GitHubCommentCreator.HEADER_TEXT)

        if (existingComment != null) {
            val updatedCommentText = appendComment(existingComment.body, commentData)

            commentClient.updateComment(existingComment, updatedCommentText)
        } else {
            val textForNewComment = createComment(commentData)

            commentClient.addComment(repository, pullRequestNumber, textForNewComment)
        }
    }
}
