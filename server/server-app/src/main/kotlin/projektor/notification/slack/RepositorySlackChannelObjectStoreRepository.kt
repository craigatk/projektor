package projektor.notification.slack

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import projektor.objectstore.json.JsonObjectStore
import projektor.persistence.objectstore.TestRunIndex

class RepositorySlackChannelObjectStoreRepository(private val store: JsonObjectStore) : RepositorySlackChannelRepository {
    override suspend fun fetchChannelName(
        repoName: String,
        projectName: String?,
    ): String? =
        withContext(Dispatchers.IO) {
            store.read<SlackChannelDocument>(slackChannelKey(repoName, projectName))?.channelName
        }

    override suspend fun upsertChannelName(
        repoName: String,
        projectName: String?,
        channelName: String,
    ) {
        withContext(Dispatchers.IO) {
            store.write(slackChannelKey(repoName, projectName), SlackChannelDocument(channelName))
        }
    }

    // Returns the number of channels deleted, like the database repository
    override suspend fun deleteChannelName(
        repoName: String,
        projectName: String?,
    ): Int =
        withContext(Dispatchers.IO) {
            val key = slackChannelKey(repoName, projectName)
            val existed = store.read<SlackChannelDocument>(key) != null

            store.delete(key)

            if (existed) 1 else 0
        }

    private fun slackChannelKey(
        repoName: String,
        projectName: String?,
    ) = "${TestRunIndex.repositoryPrefix(repoName)}slack-channel/${TestRunIndex.projectKeySegment(projectName)}.json"

    data class SlackChannelDocument(val channelName: String)
}
