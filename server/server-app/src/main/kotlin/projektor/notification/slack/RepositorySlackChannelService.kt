package projektor.notification.slack

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositorySlackChannelService(
    private val repositorySlackChannelRepository: RepositorySlackChannelRepository,
    private val slackNotificationService: SlackNotificationService?,
) {
    suspend fun fetchChannelName(
        repoName: String,
        projectName: String?,
    ): String? = repositorySlackChannelRepository.fetchChannelName(repoName, projectName)

    /**
     * Posts a confirmation message to the channel before saving so that a mistyped channel
     * or a channel the Slack app hasn't been added to is reported right away.
     */
    suspend fun saveChannelName(
        repoName: String,
        projectName: String?,
        channelName: String,
    ): String {
        val normalizedChannelName = channelName.trim().removePrefix("#")

        if (!CHANNEL_NAME_REGEX.matches(normalizedChannelName)) {
            throw InvalidSlackChannelException("Invalid Slack channel name")
        }

        if (slackNotificationService != null) {
            try {
                withContext(Dispatchers.IO) {
                    slackNotificationService.sendChannelConfiguredMessage(normalizedChannelName, repoName, projectName)
                }
            } catch (e: SlackApiException) {
                throw InvalidSlackChannelException("Unable to post to Slack channel $normalizedChannelName: ${e.message}")
            }
        }

        repositorySlackChannelRepository.upsertChannelName(repoName, projectName, normalizedChannelName)

        return normalizedChannelName
    }

    suspend fun deleteChannelName(
        repoName: String,
        projectName: String?,
    ): Int = repositorySlackChannelRepository.deleteChannelName(repoName, projectName)

    companion object {
        // Accepts both channel names and channel IDs
        private val CHANNEL_NAME_REGEX = Regex("""^[\p{L}\p{N}_-]{1,80}$""")
    }
}

class InvalidSlackChannelException(message: String) : RuntimeException(message)
