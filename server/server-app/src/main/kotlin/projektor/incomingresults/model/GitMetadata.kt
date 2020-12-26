package projektor.incomingresults.model

data class GitMetadata(
    val repoName: String?,
    val projectName: String?,
    val branchName: String?,
    val isMainBranch: Boolean,
    val commitSha: String? = null,
    val pullRequestNumber: Int? = null
)
