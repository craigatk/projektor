package projektor.repository.testrun

import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.TestSuiteData
import projektor.incomingresults.model.GitMetadata
import projektor.incomingresults.randomPublicId
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import projektor.server.api.PublicId
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import kotlin.test.assertNotNull

abstract class RepositoryTestRunRepositoryTimelineTest : RepositoryTestCase() {
    // Ten passing test cases of 2.5 seconds each
    private fun createTestRunInRepo(
        publicId: PublicId,
        repoName: String,
        ci: Boolean?,
        projectName: String?,
    ) = testRunGenerator.createTestRun(
        publicId,
        listOf(TestSuiteData("testSuite", (1..10).map { "testCase$it" }, listOf(), listOf())),
        git = GitMetadata(repoName, projectName, "main", true),
        ci = ci,
    )

    @Test
    fun `should find entries without project name for CI builds`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        val firstRunCITruePublicId = randomPublicId()
        val secondRunCINullPublicId = randomPublicId()

        val nonCIPublicId = randomPublicId()

        createTestRunInRepo(firstRunCITruePublicId, repoName, ci = true, projectName = projectName)

        createTestRunInRepo(secondRunCINullPublicId, repoName, ci = null, projectName = projectName)

        createTestRunInRepo(nonCIPublicId, repoName, ci = false, projectName = projectName)

        val timeline = runBlocking { repositoryTestRunRepository.fetchRepositoryTestRunTimeline(repoName, projectName) }
        assertNotNull(timeline)

        expectThat(timeline.timelineEntries).hasSize(1)

        val firstEntry = timeline.timelineEntries[0]
        expectThat(firstEntry) {
            get { publicId }.isEqualTo(firstRunCITruePublicId.id)
            get { totalTestCount }.isEqualTo(10)
            get { cumulativeDuration }.isEqualTo(BigDecimal("25.000"))
        }
    }

    @Test
    fun `when searching for project name null should exclude projects with name set`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        val runWithoutProjectNamePublicId = randomPublicId()
        val runWithProjectNamePublicId = randomPublicId()

        createTestRunInRepo(runWithoutProjectNamePublicId, repoName, ci = true, projectName = projectName)

        createTestRunInRepo(runWithProjectNamePublicId, repoName, ci = true, projectName = "other-project")

        val timeline = runBlocking { repositoryTestRunRepository.fetchRepositoryTestRunTimeline(repoName, projectName) }
        assertNotNull(timeline)

        expectThat(timeline.timelineEntries).hasSize(1)

        expectThat(timeline.timelineEntries[0]) {
            get { publicId }.isEqualTo(runWithoutProjectNamePublicId.id)
        }
    }

    @Test
    fun `when searching for specific project name should exclude projects with null or with different names`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = "my-project"

        val runWithoutProjectNamePublicId = randomPublicId()
        val runWithProjectNamePublicId = randomPublicId()
        val runWithDifferentProjectNamePublicId = randomPublicId()

        createTestRunInRepo(runWithoutProjectNamePublicId, repoName, ci = true, projectName = null)

        createTestRunInRepo(runWithProjectNamePublicId, repoName, ci = true, projectName = projectName)

        createTestRunInRepo(runWithDifferentProjectNamePublicId, repoName, ci = true, projectName = "other-project")

        val timeline = runBlocking { repositoryTestRunRepository.fetchRepositoryTestRunTimeline(repoName, projectName) }
        assertNotNull(timeline)

        expectThat(timeline.timelineEntries).hasSize(1)

        expectThat(timeline.timelineEntries[0]) {
            get { publicId }.isEqualTo(runWithProjectNamePublicId.id)
        }
    }
}

class RepositoryTestRunRepositoryTimelineDatabaseTest : RepositoryTestRunRepositoryTimelineTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class RepositoryTestRunRepositoryTimelineObjectStoreTest : RepositoryTestRunRepositoryTimelineTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
