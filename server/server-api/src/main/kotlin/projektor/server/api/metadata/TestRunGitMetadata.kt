package projektor.server.api.metadata

data class TestRunGitMetadata(
    val repoName: String?,
    val orgName: String?,
    val branchName: String?,
    val projectName: String?,
    val isMainBranch: Boolean,
    val pullRequestNumber: Int?,
    val commitSha: String?,
) {
    var gitHubBaseUrl: String? = null
}
