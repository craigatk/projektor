package projektor.notification.slack

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.test.inject
import projektor.DatabaseRepositoryTestCase
import projektor.util.randomOrgAndRepo
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class RepositorySlackChannelDatabaseRepositoryTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should save, update, and delete channel for repo`() {
        val repositorySlackChannelRepository by inject<RepositorySlackChannelRepository>()

        val repoName = randomOrgAndRepo()

        runBlocking {
            expectThat(repositorySlackChannelRepository.fetchChannelName(repoName, null)).isNull()

            repositorySlackChannelRepository.upsertChannelName(repoName, null, "first-channel")
            expectThat(repositorySlackChannelRepository.fetchChannelName(repoName, null)).isEqualTo("first-channel")

            repositorySlackChannelRepository.upsertChannelName(repoName, null, "second-channel")
            expectThat(repositorySlackChannelRepository.fetchChannelName(repoName, null)).isEqualTo("second-channel")

            expectThat(repositorySlackChannelRepository.deleteChannelName(repoName, null)).isEqualTo(1)
            expectThat(repositorySlackChannelRepository.fetchChannelName(repoName, null)).isNull()
        }
    }

    @Test
    fun `should keep channels for projects in the same repo separate`() {
        val repositorySlackChannelRepository by inject<RepositorySlackChannelRepository>()

        val repoName = randomOrgAndRepo()

        runBlocking {
            repositorySlackChannelRepository.upsertChannelName(repoName, null, "repo-channel")
            repositorySlackChannelRepository.upsertChannelName(repoName, "ui", "ui-channel")
            repositorySlackChannelRepository.upsertChannelName(repoName, "server", "server-channel")

            expectThat(repositorySlackChannelRepository.fetchChannelName(repoName, null)).isEqualTo("repo-channel")
            expectThat(repositorySlackChannelRepository.fetchChannelName(repoName, "ui")).isEqualTo("ui-channel")
            expectThat(repositorySlackChannelRepository.fetchChannelName(repoName, "server")).isEqualTo("server-channel")
            expectThat(repositorySlackChannelRepository.fetchChannelName(repoName, "other")).isNull()

            repositorySlackChannelRepository.deleteChannelName(repoName, "ui")

            expectThat(repositorySlackChannelRepository.fetchChannelName(repoName, "ui")).isNull()
            expectThat(repositorySlackChannelRepository.fetchChannelName(repoName, null)).isEqualTo("repo-channel")
            expectThat(repositorySlackChannelRepository.fetchChannelName(repoName, "server")).isEqualTo("server-channel")
        }
    }
}
