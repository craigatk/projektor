package projektor.repository.coverage

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.repository.coverage.RepositoryCoverageTimeline
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.server.example.coverage.JacocoXmlLoader.Companion.jacocoXmlParserLineCoveragePercentage
import projektor.server.example.coverage.JacocoXmlLoader.Companion.serverAppLineCoveragePercentage
import projektor.server.example.coverage.JacocoXmlLoader.Companion.serverAppReducedLineCoveragePercentage
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.time.ZoneOffset
import kotlin.test.assertNotNull

class RepositoryCoverageTimelineApplicationTest : ApplicationTestCase() {

    @Test
    fun `should fetch coverage timeline in mainline for repository without project name`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"

        val firstRunPublicId = randomPublicId()
        val secondRunPublicId = randomPublicId()
        val thirdRunPublicId = randomPublicId()

        val runInDifferentBranchPublicId = randomPublicId()
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
                    publicId = runInDifferentBranchPublicId,
                    coverageText = JacocoXmlLoader().jacocoXmlParser(),
                    repoName = repoName,
                    branchName = "feature/dev"
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
                    get { createdTimestamp }.isEqualTo(firstTestRunDB.createdTimestamp.toInstant(ZoneOffset.UTC))
                    get { coverageStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(serverAppReducedLineCoveragePercentage)
                }

                val secondTestRunDB = testRunDao.fetchOneByPublicId(secondRunPublicId.id)
                val secondTimelineEntry = coverageTimeline.timelineEntries.find { it.publicId == secondRunPublicId.id }
                assertNotNull(secondTimelineEntry)

                expectThat(secondTimelineEntry) {
                    get { createdTimestamp }.isEqualTo(secondTestRunDB.createdTimestamp.toInstant(ZoneOffset.UTC))
                    get { coverageStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(serverAppLineCoveragePercentage)
                }

                val thirdTestRunDB = testRunDao.fetchOneByPublicId(thirdRunPublicId.id)
                val thirdTimelineEntry = coverageTimeline.timelineEntries.find { it.publicId == thirdRunPublicId.id }
                assertNotNull(thirdTimelineEntry)

                expectThat(thirdTimelineEntry) {
                    get { createdTimestamp }.isEqualTo(thirdTestRunDB.createdTimestamp.toInstant(ZoneOffset.UTC))
                    get { coverageStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(jacocoXmlParserLineCoveragePercentage)
                }
            }
        }
    }

    @Test
    fun `should fetch coverage timeline in all branches for repository without project name`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"

        val firstRunPublicId = randomPublicId()
        val secondRunPublicId = randomPublicId()
        val thirdRunPublicId = randomPublicId()
        val runInDifferentBranchPublicId = randomPublicId()

        val runInDifferentProjectPublicId = randomPublicId()
        val runInDifferentRepoPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/coverage/timeline?branch=ALL") {

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
                    publicId = runInDifferentBranchPublicId,
                    coverageText = JacocoXmlLoader().jacocoXmlParser(),
                    repoName = repoName,
                    branchName = "feature/dev"
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

                expectThat(coverageTimeline.timelineEntries)
                    .hasSize(4)
                    .any {
                        get { publicId }.isEqualTo(firstRunPublicId.id)
                    }
                    .any {
                        get { publicId }.isEqualTo(secondRunPublicId.id)
                    }
                    .any {
                        get { publicId }.isEqualTo(thirdRunPublicId.id)
                    }
                    .any {
                        get { publicId }.isEqualTo(runInDifferentBranchPublicId.id)
                    }
            }
        }
    }

    @Test
    fun `should fetch coverage timeline in mainline for repository with project name`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = "server-project"

        val firstRunPublicId = randomPublicId()
        val secondRunPublicId = randomPublicId()

        val runInDifferentBranch = randomPublicId()
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
                    publicId = runInDifferentBranch,
                    coverageText = JacocoXmlLoader().serverApp(),
                    repoName = repoName,
                    branchName = "feature/dev",
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
                    get { createdTimestamp }.isEqualTo(firstTestRunDB.createdTimestamp.toInstant(ZoneOffset.UTC))
                    get { coverageStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(serverAppReducedLineCoveragePercentage)
                }

                val secondTestRunDB = testRunDao.fetchOneByPublicId(secondRunPublicId.id)
                val secondTimelineEntry = coverageTimeline.timelineEntries.find { it.publicId == secondRunPublicId.id }
                assertNotNull(secondTimelineEntry)

                expectThat(secondTimelineEntry) {
                    get { createdTimestamp }.isEqualTo(secondTestRunDB.createdTimestamp.toInstant(ZoneOffset.UTC))
                    get { coverageStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(serverAppLineCoveragePercentage)
                }
            }
        }
    }

    @Test
    fun `should fetch coverage timeline in all branches for repository with project name`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = "server-project"

        val firstRunPublicId = randomPublicId()
        val secondRunPublicId = randomPublicId()
        val runInDifferentBranch = randomPublicId()

        val runInRepoWithoutProjectPublicId = randomPublicId()
        val runInDifferentProjectPublicId = randomPublicId()
        val runInDifferentRepoPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/project/$projectName/coverage/timeline?branch=ALL") {

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
                    publicId = runInDifferentBranch,
                    coverageText = JacocoXmlLoader().serverApp(),
                    repoName = repoName,
                    branchName = "feature/dev",
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

                expectThat(coverageTimeline.timelineEntries)
                    .hasSize(3)
                    .any {
                        get { publicId }.isEqualTo(firstRunPublicId.id)
                    }
                    .any {
                        get { publicId }.isEqualTo(secondRunPublicId.id)
                    }
                    .any {
                        get { publicId }.isEqualTo(runInDifferentBranch.id)
                    }
            }
        }
    }
}
