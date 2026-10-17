package projektor.repository.testrun

import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.incomingresults.randomPublicId
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

abstract class RepositoryTestRunRepositoryRecentRunsTest : RepositoryTestCase() {
    @Test
    fun `should find recent test run public IDs`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        val tooOld = randomPublicId()
        val inRangeOlder = randomPublicId()
        val inRangeNewer = randomPublicId()

        testRunGenerator.createSimpleTestRunInRepo(tooOld, repoName, true, projectName)
        testRunGenerator.createSimpleTestRunInRepo(inRangeOlder, repoName, true, projectName)
        testRunGenerator.createSimpleTestRunInRepo(inRangeNewer, repoName, true, projectName)

        val recentIds = runBlocking { repositoryTestRunRepository.fetchRecentTestRunPublicIds(repoName, projectName, 2) }

        expectThat(recentIds).hasSize(2)
        expectThat(recentIds[0]).isEqualTo(inRangeNewer)
        expectThat(recentIds[1]).isEqualTo(inRangeOlder)
    }

    @Test
    fun `should find count of test runs`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        repeat(5) {
            testRunGenerator.createSimpleTestRunInRepo(randomPublicId(), repoName, true, projectName)
        }

        val runCount = runBlocking { repositoryTestRunRepository.fetchTestRunCount(repoName, projectName) }

        expectThat(runCount).isEqualTo(5)
    }
}

class RepositoryTestRunRepositoryRecentRunsDatabaseTest : RepositoryTestRunRepositoryRecentRunsTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class RepositoryTestRunRepositoryRecentRunsObjectStoreTest : RepositoryTestRunRepositoryRecentRunsTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
