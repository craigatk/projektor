package projektor.incomingresults.model

data class GitMetadata(
    val repoName: String?,
    val projectName: String?,
    val branchName: String?,
    val isMainBranch: Boolean
)
