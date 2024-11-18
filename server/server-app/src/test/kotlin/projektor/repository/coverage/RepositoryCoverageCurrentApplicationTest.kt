package projektor.repository.coverage

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.repository.coverage.RepositoryCurrentCoverage
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.server.example.coverage.JacocoXmlLoader.Companion.jacocoXmlParserLineCoveragePercentage
import projektor.server.example.coverage.JacocoXmlLoader.Companion.serverAppLineCoveragePercentage
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.ZoneOffset
import kotlin.test.assertNotNull

class RepositoryCoverageCurrentApplicationTest : ApplicationTestCase() {
    @Test
    fun `should fetch current coverage for repository without project name`() =
        projektorTestApplication {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "$orgName/repo"

            val firstRunPublicId = randomPublicId()
            val secondRunPublicId = randomPublicId()
            val thirdRunPublicId = randomPublicId()

            val runInDifferentProjectPublicId = randomPublicId()
            val runInDifferentRepoPublicId = randomPublicId()

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = firstRunPublicId,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repoName,
                branchName = "main",
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = secondRunPublicId,
                coverageText = JacocoXmlLoader().serverApp(),
                repoName = repoName,
                branchName = "main",
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = thirdRunPublicId,
                coverageText = JacocoXmlLoader().jacocoXmlParser(),
                repoName = repoName,
                branchName = "main",
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = runInDifferentProjectPublicId,
                coverageText = JacocoXmlLoader().junitResultsParser(),
                repoName = repoName,
                branchName = "main",
                projectName = "other-project",
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = runInDifferentRepoPublicId,
                coverageText = JacocoXmlLoader().junitResultsParser(),
                repoName = "other/repo",
                branchName = "main",
            )

            val response = client.get("/repo/$repoName/coverage/current")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            val responseBody = response.bodyAsText()
            assertNotNull(responseBody)

            val currentCoverage: RepositoryCurrentCoverage = objectMapper.readValue(responseBody)

            val thirdTestRunDB = testRunDao.fetchOneByPublicId(thirdRunPublicId.id)

            expectThat(currentCoverage) {
                get { createdTimestamp }.isEqualTo(thirdTestRunDB.createdTimestamp.toInstant(ZoneOffset.UTC))
                get { coveredPercentage }.isEqualTo(jacocoXmlParserLineCoveragePercentage)
            }
        }

    @Test
    fun `should fetch current coverage for repository with project name`() =
        projektorTestApplication {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "$orgName/repo"
            val projectName = "server-project"

            val firstRunPublicId = randomPublicId()
            val secondRunPublicId = randomPublicId()

            val runInRepoWithoutProjectPublicId = randomPublicId()
            val runInDifferentProjectPublicId = randomPublicId()
            val runInDifferentRepoPublicId = randomPublicId()

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = firstRunPublicId,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repoName,
                branchName = "main",
                projectName = projectName,
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = secondRunPublicId,
                coverageText = JacocoXmlLoader().serverApp(),
                repoName = repoName,
                branchName = "main",
                projectName = projectName,
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = runInRepoWithoutProjectPublicId,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repoName,
                branchName = "main",
                projectName = null,
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = runInDifferentProjectPublicId,
                coverageText = JacocoXmlLoader().junitResultsParser(),
                repoName = repoName,
                branchName = "main",
                projectName = "other-project",
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = runInDifferentRepoPublicId,
                coverageText = JacocoXmlLoader().junitResultsParser(),
                repoName = "other/repo",
                branchName = "main",
            )

            val response = client.get("/repo/$repoName/project/$projectName/coverage/current")
            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            val responseBody = response.bodyAsText()
            assertNotNull(responseBody)

            val currentCoverage: RepositoryCurrentCoverage = objectMapper.readValue(responseBody)

            val secondTestRunDB = testRunDao.fetchOneByPublicId(secondRunPublicId.id)

            expectThat(currentCoverage) {
                get { createdTimestamp }.isEqualTo(secondTestRunDB.createdTimestamp.toInstant(ZoneOffset.UTC))
                get { coveredPercentage }.isEqualTo(serverAppLineCoveragePercentage)
            }
        }

    @Test
    fun `when no mainline reports with coverage should return 204`() =
        projektorTestApplication {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "$orgName/repo"

            val runInRepoFeatureBranch = randomPublicId()
            val runInDifferentProjectPublicId = randomPublicId()
            val runInDifferentRepoPublicId = randomPublicId()

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = runInRepoFeatureBranch,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repoName,
                branchName = "another-branch",
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = runInDifferentProjectPublicId,
                coverageText = JacocoXmlLoader().junitResultsParser(),
                repoName = repoName,
                branchName = "main",
                projectName = "other-project",
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = runInDifferentRepoPublicId,
                coverageText = JacocoXmlLoader().junitResultsParser(),
                repoName = "other/repo",
                branchName = "main",
            )

            val response = client.get("/repo/$repoName/coverage/current")

            expectThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        }
}
