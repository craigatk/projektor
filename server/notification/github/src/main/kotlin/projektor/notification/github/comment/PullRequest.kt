package projektor.notification.github.comment

data class PullRequest(
    val orgName: String,
    val repoName: String,
    val number: Int,
)
