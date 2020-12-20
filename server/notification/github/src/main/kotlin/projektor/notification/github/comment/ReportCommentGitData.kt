package projektor.notification.github.comment

data class ReportCommentGitData(
    val orgName: String,
    val repoName: String,
    val branchName: String,
    val commitSha: String? = null
)
