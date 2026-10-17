package projektor.repository.testrun

import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.incomingresults.randomPublicId
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import projektor.persistence.TestRunGenerator
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

abstract class RepositoryTestRunRepositoryPullRequestTest : RepositoryTestCase() {
    @Test
    fun `should find most recent test run public ID for a pull request`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val pullRequestNumber = 42

        val olderRun = randomPublicId()
        val newerRun = randomPublicId()

        testRunGenerator.createTestRunInRepo(
            olderRun,
            listOf(TestRunGenerator.simpleTestSuite()),
            repoName,
            true,
            null,
            branchName = "feature",
            pullRequestNumber = pullRequestNumber,
        )

        testRunGenerator.createTestRunInRepo(
            newerRun,
            listOf(TestRunGenerator.simpleTestSuite()),
            repoName,
            true,
            null,
            branchName = "feature",
            pullRequestNumber = pullRequestNumber,
        )

        val otherPullRequestRun = randomPublicId()
        testRunGenerator.createTestRunInRepo(
            otherPullRequestRun,
            listOf(TestRunGenerator.simpleTestSuite()),
            repoName,
            true,
            null,
            branchName = "other-feature",
            pullRequestNumber = 99,
        )

        val mostRecentPublicId =
            runBlocking { repositoryTestRunRepository.fetchMostRecentTestRunPublicId(repoName, pullRequestNumber) }

        expectThat(mostRecentPublicId).isEqualTo(newerRun)
    }

    @Test
    fun `should return null when no test run found for pull request`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"

        val mostRecentPublicId =
            runBlocking { repositoryTestRunRepository.fetchMostRecentTestRunPublicId(repoName, 123) }

        expectThat(mostRecentPublicId).isNull()
    }
}

class RepositoryTestRunRepositoryPullRequestDatabaseTest : RepositoryTestRunRepositoryPullRequestTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class RepositoryTestRunRepositoryPullRequestObjectStoreTest : RepositoryTestRunRepositoryPullRequestTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
