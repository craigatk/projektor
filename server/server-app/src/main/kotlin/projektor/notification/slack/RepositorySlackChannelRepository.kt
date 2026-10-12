package projektor.notification.slack

interface RepositorySlackChannelRepository {
    suspend fun fetchChannelName(
        repoName: String,
        projectName: String?,
    ): String?

    suspend fun upsertChannelName(
        repoName: String,
        projectName: String?,
        channelName: String,
    )

    suspend fun deleteChannelName(
        repoName: String,
        projectName: String?,
    ): Int
}
