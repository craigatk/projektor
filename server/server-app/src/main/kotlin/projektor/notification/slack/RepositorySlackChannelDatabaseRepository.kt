package projektor.notification.slack

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import projektor.database.generated.Tables.REPOSITORY_SLACK_CHANNEL
import java.time.LocalDateTime

class RepositorySlackChannelDatabaseRepository(private val dslContext: DSLContext) : RepositorySlackChannelRepository {
    override suspend fun fetchChannelName(
        repoName: String,
        projectName: String?,
    ): String? =
        withContext(Dispatchers.IO) {
            dslContext.select(REPOSITORY_SLACK_CHANNEL.CHANNEL_NAME)
                .from(REPOSITORY_SLACK_CHANNEL)
                .where(
                    REPOSITORY_SLACK_CHANNEL.REPO_NAME.eq(repoName)
                        .and(REPOSITORY_SLACK_CHANNEL.PROJECT_NAME.eq(normalizeProjectName(projectName))),
                )
                .fetchOne(REPOSITORY_SLACK_CHANNEL.CHANNEL_NAME)
        }

    override suspend fun upsertChannelName(
        repoName: String,
        projectName: String?,
        channelName: String,
    ) = withContext(Dispatchers.IO) {
        dslContext.insertInto(REPOSITORY_SLACK_CHANNEL)
            .set(REPOSITORY_SLACK_CHANNEL.REPO_NAME, repoName)
            .set(REPOSITORY_SLACK_CHANNEL.PROJECT_NAME, normalizeProjectName(projectName))
            .set(REPOSITORY_SLACK_CHANNEL.CHANNEL_NAME, channelName)
            .onConflict(REPOSITORY_SLACK_CHANNEL.REPO_NAME, REPOSITORY_SLACK_CHANNEL.PROJECT_NAME)
            .doUpdate()
            .set(REPOSITORY_SLACK_CHANNEL.CHANNEL_NAME, channelName)
            .set(REPOSITORY_SLACK_CHANNEL.UPDATED_TIMESTAMP, LocalDateTime.now())
            .execute()

        Unit
    }

    override suspend fun deleteChannelName(
        repoName: String,
        projectName: String?,
    ): Int =
        withContext(Dispatchers.IO) {
            dslContext.deleteFrom(REPOSITORY_SLACK_CHANNEL)
                .where(
                    REPOSITORY_SLACK_CHANNEL.REPO_NAME.eq(repoName)
                        .and(REPOSITORY_SLACK_CHANNEL.PROJECT_NAME.eq(normalizeProjectName(projectName))),
                )
                .execute()
        }

    // Project name is stored as an empty string when not set so the (repo_name, project_name) unique constraint applies
    private fun normalizeProjectName(projectName: String?): String = projectName ?: ""
}
