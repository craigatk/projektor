package projektor.repository.testrun

import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class RepositoryTestRunDatabaseRepositoryPullRequestTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should find most recent test run public ID for a pull request`() {
        val repositoryTestRunDatabaseRepository = RepositoryTestRunDatabaseRepository(dslContext)

        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val pullRequestNumber = 42

        val olderRun = randomPublicId()
        val newerRun = randomPublicId()

        val olderTestRunDB = testRunDBGenerator.createSimpleTestRun(olderRun)
        testRunDBGenerator.addResultsMetadata(olderTestRunDB, true)
        testRunDBGenerator.addGitMetadata(olderTestRunDB, repoName, false, "feature", null, pullRequestNumber, null)

        val newerTestRunDB = testRunDBGenerator.createSimpleTestRun(newerRun)
        testRunDBGenerator.addResultsMetadata(newerTestRunDB, true)
        testRunDBGenerator.addGitMetadata(newerTestRunDB, repoName, false, "feature", null, pullRequestNumber, null)

        val otherPullRequestRun = randomPublicId()
        val otherTestRunDB = testRunDBGenerator.createSimpleTestRun(otherPullRequestRun)
        testRunDBGenerator.addResultsMetadata(otherTestRunDB, true)
        testRunDBGenerator.addGitMetadata(otherTestRunDB, repoName, false, "other-feature", null, 99, null)

        val mostRecentPublicId =
            runBlocking { repositoryTestRunDatabaseRepository.fetchMostRecentTestRunPublicId(repoName, pullRequestNumber) }

        expectThat(mostRecentPublicId).isEqualTo(newerRun)
    }

    @Test
    fun `should return null when no test run found for pull request`() {
        val repositoryTestRunDatabaseRepository = RepositoryTestRunDatabaseRepository(dslContext)

        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"

        val mostRecentPublicId =
            runBlocking { repositoryTestRunDatabaseRepository.fetchMostRecentTestRunPublicId(repoName, 123) }

        expectThat(mostRecentPublicId).isNull()
    }
}
