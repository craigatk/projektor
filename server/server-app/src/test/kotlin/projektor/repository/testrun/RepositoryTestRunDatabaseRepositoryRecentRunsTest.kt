package projektor.repository.testrun

import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class RepositoryTestRunDatabaseRepositoryRecentRunsTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should find recent test run public IDs`() {
        val repositoryTestRunDatabaseRepository = RepositoryTestRunDatabaseRepository(dslContext)

        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        val tooOld = randomPublicId()
        val inRangeOlder = randomPublicId()
        val inRangeNewer = randomPublicId()

        testRunDBGenerator.createSimpleTestRunInRepo(tooOld, repoName, true, projectName)
        testRunDBGenerator.createSimpleTestRunInRepo(inRangeOlder, repoName, true, projectName)
        testRunDBGenerator.createSimpleTestRunInRepo(inRangeNewer, repoName, true, projectName)

        val recentIds = runBlocking { repositoryTestRunDatabaseRepository.fetchRecentTestRunPublicIds(repoName, projectName, 2) }

        expectThat(recentIds).hasSize(2)
        expectThat(recentIds[0]).isEqualTo(inRangeNewer)
        expectThat(recentIds[1]).isEqualTo(inRangeOlder)
    }

    @Test
    fun `should find count of test runs`() {
        val repositoryTestRunDatabaseRepository = RepositoryTestRunDatabaseRepository(dslContext)

        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        repeat(5) {
            testRunDBGenerator.createSimpleTestRunInRepo(randomPublicId(), repoName, true, projectName)
        }

        val runCount = runBlocking { repositoryTestRunDatabaseRepository.fetchTestRunCount(repoName, projectName) }

        expectThat(runCount).isEqualTo(5)
    }
}
