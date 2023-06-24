package projektor.repository.testrun

import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.createTestRun
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import kotlin.test.assertNotNull

class RepositoryTestRunDatabaseRepositoryTimelineTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should find entries without project name for CI builds`() {
        val repositoryTestRunDatabaseRepository = RepositoryTestRunDatabaseRepository(dslContext)

        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        val firstRunCITruePublicId = randomPublicId()
        val secondRunCINullPublicId = randomPublicId()

        val nonCIPublicId = randomPublicId()

        val firstTestRun = createTestRun(firstRunCITruePublicId, 20, BigDecimal("25.000"))
        testRunDao.insert(firstTestRun)
        testRunDBGenerator.addResultsMetadata(firstTestRun, true)
        testRunDBGenerator.addGitMetadata(firstTestRun, repoName, true, "main", projectName, null, null)

        val secondTestRun = createTestRun(secondRunCINullPublicId, 30, BigDecimal("35.000"))
        testRunDao.insert(secondTestRun)
        testRunDBGenerator.addGitMetadata(secondTestRun, repoName, true, "main", projectName, null, null)

        val nonCITestRun = createTestRun(nonCIPublicId, 20, BigDecimal("25.000"))
        testRunDao.insert(nonCITestRun)
        testRunDBGenerator.addResultsMetadata(nonCITestRun, false)
        testRunDBGenerator.addGitMetadata(nonCITestRun, repoName, true, "main", projectName, null, null)

        val timeline = runBlocking { repositoryTestRunDatabaseRepository.fetchRepositoryTestRunTimeline(repoName, projectName) }
        assertNotNull(timeline)

        expectThat(timeline.timelineEntries).hasSize(1)

        val firstEntry = timeline.timelineEntries[0]
        expectThat(firstEntry) {
            get { publicId }.isEqualTo(firstRunCITruePublicId.id)
            get { totalTestCount }.isEqualTo(20)
            get { cumulativeDuration }.isEqualTo(BigDecimal("25.000"))
        }
    }

    @Test
    fun `when searching for project name null should exclude projects with name set`() {
        val repositoryTestRunDatabaseRepository = RepositoryTestRunDatabaseRepository(dslContext)

        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        val runWithoutProjectNamePublicId = randomPublicId()
        val runWithProjectNamePublicId = randomPublicId()

        val testRunWithoutProjectName = createTestRun(runWithoutProjectNamePublicId, 20, BigDecimal("25.000"))
        testRunDao.insert(testRunWithoutProjectName)
        testRunDBGenerator.addResultsMetadata(testRunWithoutProjectName, true)
        testRunDBGenerator.addGitMetadata(testRunWithoutProjectName, repoName, true, "main", projectName, null, null)

        val testRunWithProjectName = createTestRun(runWithProjectNamePublicId, 30, BigDecimal("35.000"))
        testRunDao.insert(testRunWithProjectName)
        testRunDBGenerator.addResultsMetadata(testRunWithProjectName, true)
        testRunDBGenerator.addGitMetadata(testRunWithProjectName, repoName, true, "main", "other-project", null, null)

        val timeline = runBlocking { repositoryTestRunDatabaseRepository.fetchRepositoryTestRunTimeline(repoName, projectName) }
        assertNotNull(timeline)

        expectThat(timeline.timelineEntries).hasSize(1)

        expectThat(timeline.timelineEntries[0]) {
            get { publicId }.isEqualTo(runWithoutProjectNamePublicId.id)
        }
    }

    @Test
    fun `when searching for specific project name should exclude projects with null or with different names`() {
        val repositoryTestRunDatabaseRepository = RepositoryTestRunDatabaseRepository(dslContext)

        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = "my-project"

        val runWithoutProjectNamePublicId = randomPublicId()
        val runWithProjectNamePublicId = randomPublicId()
        val runWithDifferentProjectNamePublicId = randomPublicId()

        val testRunWithoutProjectName = createTestRun(runWithoutProjectNamePublicId, 20, BigDecimal("25.000"))
        testRunDao.insert(testRunWithoutProjectName)
        testRunDBGenerator.addResultsMetadata(testRunWithoutProjectName, true)
        testRunDBGenerator.addGitMetadata(testRunWithoutProjectName, repoName, true, "main", null, null, null)

        val testRunWithProjectName = createTestRun(runWithProjectNamePublicId, 30, BigDecimal("35.000"))
        testRunDao.insert(testRunWithProjectName)
        testRunDBGenerator.addResultsMetadata(testRunWithProjectName, true)
        testRunDBGenerator.addGitMetadata(testRunWithProjectName, repoName, true, "main", projectName, null, null)

        val testRunWithDifferentProjectName = createTestRun(runWithDifferentProjectNamePublicId, 40, BigDecimal("45.000"))
        testRunDao.insert(testRunWithDifferentProjectName)
        testRunDBGenerator.addResultsMetadata(testRunWithDifferentProjectName, true)
        testRunDBGenerator.addGitMetadata(testRunWithDifferentProjectName, repoName, true, "main", "other-project", null, null)

        val timeline = runBlocking { repositoryTestRunDatabaseRepository.fetchRepositoryTestRunTimeline(repoName, projectName) }
        assertNotNull(timeline)

        expectThat(timeline.timelineEntries).hasSize(1)

        expectThat(timeline.timelineEntries[0]) {
            get { publicId }.isEqualTo(runWithProjectNamePublicId.id)
        }
    }
}
