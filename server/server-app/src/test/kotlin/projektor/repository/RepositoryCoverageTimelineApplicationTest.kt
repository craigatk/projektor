package projektor.repository

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import kotlin.test.assertNotNull
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.repository.RepositoryCoverageTimeline
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.server.example.coverage.JacocoXmlLoader.Companion.jacocoXmlParserLineCoveragePercentage
import projektor.server.example.coverage.JacocoXmlLoader.Companion.serverAppLineCoveragePercentage
import projektor.server.example.coverage.JacocoXmlLoader.Companion.serverAppReducedLineCoveragePercentage
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

@KtorExperimentalAPI
class RepositoryCoverageTimelineApplicationTest : ApplicationTestCase() {
    @Test
    fun `should fetch coverage timeline for repository without project name`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"

        val firstRunPublicId = randomPublicId()
        val secondRunPublicId = randomPublicId()
        val thirdRunPublicId = randomPublicId()

        val runInDifferentProjectPublicId = randomPublicId()
        val runInDifferentRepoPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/coverage/timeline") {

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                        publicId = firstRunPublicId,
                        coverageText = JacocoXmlLoader().serverAppReduced(),
                        repoName = repoName,
                        branchName = "main"
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                        publicId = secondRunPublicId,
                        coverageText = JacocoXmlLoader().serverApp(),
                        repoName = repoName,
                        branchName = "main"
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                        publicId = thirdRunPublicId,
                        coverageText = JacocoXmlLoader().jacocoXmlParser(),
                        repoName = repoName,
                        branchName = "main"
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                        publicId = runInDifferentProjectPublicId,
                        coverageText = JacocoXmlLoader().junitResultsParser(),
                        repoName = repoName,
                        branchName = "main",
                        projectName = "other-project"
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                        publicId = runInDifferentRepoPublicId,
                        coverageText = JacocoXmlLoader().junitResultsParser(),
                        repoName = "other/repo",
                        branchName = "main"
                )
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverageTimeline = objectMapper.readValue(response.content, RepositoryCoverageTimeline::class.java)
                assertNotNull(coverageTimeline)

                expectThat(coverageTimeline.timelineEntries).hasSize(3)

                val firstTestRunDB = testRunDao.fetchOneByPublicId(firstRunPublicId.id)
                val firstTimelineEntry = coverageTimeline.timelineEntries.find { it.publicId == firstRunPublicId.id }
                assertNotNull(firstTimelineEntry)

                expectThat(firstTimelineEntry) {
                    get { createdTimestamp }.isEqualTo(firstTestRunDB.createdTimestamp.toInstant())
                    get { coverageStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(serverAppReducedLineCoveragePercentage)
                }

                val secondTestRunDB = testRunDao.fetchOneByPublicId(secondRunPublicId.id)
                val secondTimelineEntry = coverageTimeline.timelineEntries.find { it.publicId == secondRunPublicId.id }
                assertNotNull(secondTimelineEntry)

                expectThat(secondTimelineEntry) {
                    get { createdTimestamp }.isEqualTo(secondTestRunDB.createdTimestamp.toInstant())
                    get { coverageStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(serverAppLineCoveragePercentage)
                }

                val thirdTestRunDB = testRunDao.fetchOneByPublicId(thirdRunPublicId.id)
                val thirdTimelineEntry = coverageTimeline.timelineEntries.find { it.publicId == thirdRunPublicId.id }
                assertNotNull(thirdTimelineEntry)

                expectThat(thirdTimelineEntry) {
                    get { createdTimestamp }.isEqualTo(thirdTestRunDB.createdTimestamp.toInstant())
                    get { coverageStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(jacocoXmlParserLineCoveragePercentage)
                }
            }
        }
    }

    @Test
    fun `should fetch coverage timeline for repository with project name`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = "server-project"

        val firstRunPublicId = randomPublicId()
        val secondRunPublicId = randomPublicId()

        val runInRepoWithoutProjectPublicId = randomPublicId()
        val runInDifferentProjectPublicId = randomPublicId()
        val runInDifferentRepoPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/project/$projectName/coverage/timeline") {

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                        publicId = firstRunPublicId,
                        coverageText = JacocoXmlLoader().serverAppReduced(),
                        repoName = repoName,
                        branchName = "main",
                        projectName = projectName
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                        publicId = secondRunPublicId,
                        coverageText = JacocoXmlLoader().serverApp(),
                        repoName = repoName,
                        branchName = "main",
                        projectName = projectName
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                        publicId = runInRepoWithoutProjectPublicId,
                        coverageText = JacocoXmlLoader().serverAppReduced(),
                        repoName = repoName,
                        branchName = "main",
                        projectName = null
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                        publicId = runInDifferentProjectPublicId,
                        coverageText = JacocoXmlLoader().junitResultsParser(),
                        repoName = repoName,
                        branchName = "main",
                        projectName = "other-project"
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                        publicId = runInDifferentRepoPublicId,
                        coverageText = JacocoXmlLoader().junitResultsParser(),
                        repoName = "other/repo",
                        branchName = "main"
                )
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverageTimeline = objectMapper.readValue(response.content, RepositoryCoverageTimeline::class.java)
                assertNotNull(coverageTimeline)

                expectThat(coverageTimeline.timelineEntries).hasSize(2)

                val firstTestRunDB = testRunDao.fetchOneByPublicId(firstRunPublicId.id)
                val firstTimelineEntry = coverageTimeline.timelineEntries.find { it.publicId == firstRunPublicId.id }
                assertNotNull(firstTimelineEntry)

                expectThat(firstTimelineEntry) {
                    get { createdTimestamp }.isEqualTo(firstTestRunDB.createdTimestamp.toInstant())
                    get { coverageStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(serverAppReducedLineCoveragePercentage)
                }

                val secondTestRunDB = testRunDao.fetchOneByPublicId(secondRunPublicId.id)
                val secondTimelineEntry = coverageTimeline.timelineEntries.find { it.publicId == secondRunPublicId.id }
                assertNotNull(secondTimelineEntry)

                expectThat(secondTimelineEntry) {
                    get { createdTimestamp }.isEqualTo(secondTestRunDB.createdTimestamp.toInstant())
                    get { coverageStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(serverAppLineCoveragePercentage)
                }
            }
        }
    }
}
