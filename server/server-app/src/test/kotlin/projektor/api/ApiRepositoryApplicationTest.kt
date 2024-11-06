package projektor.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.repository.coverage.RepositoryCurrentCoverage
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.util.randomFullRepoName
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.ZoneOffset
import kotlin.test.assertNotNull

class ApiRepositoryApplicationTest : ApplicationTestCase() {
    override fun autoStartServer() = true

    @Test
    fun `should fetch current coverage for repository without project name`() =
        testSuspend {
            val repoName = randomFullRepoName()

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

            val response = testClient.get("/api/v1/repo/$repoName/coverage/current")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            val responseBody = response.bodyAsText()
            assertNotNull(responseBody)

            val currentCoverage: RepositoryCurrentCoverage = objectMapper.readValue(responseBody)

            val thirdTestRunDB = testRunDao.fetchOneByPublicId(thirdRunPublicId.id)

            expectThat(currentCoverage) {
                get { createdTimestamp }.isEqualTo(thirdTestRunDB.createdTimestamp.toInstant(ZoneOffset.UTC))
                get { coveredPercentage }.isEqualTo(JacocoXmlLoader.jacocoXmlParserLineCoveragePercentage)
                get { repo }.isEqualTo(repoName)
            }
        }

    @Test
    fun `should fetch current coverage for repository with project name`() =
        testSuspend {
            val repoName = randomFullRepoName()

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

            val response = testClient.get("/api/v1/repo/$repoName/coverage/current?project=other-project")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            val responseBody = response.bodyAsText()
            assertNotNull(responseBody)

            val currentCoverage: RepositoryCurrentCoverage = objectMapper.readValue(responseBody)

            val currentCoverageTestRunDB = testRunDao.fetchOneByPublicId(runInDifferentProjectPublicId.id)

            expectThat(currentCoverage) {
                get { id }.isEqualTo(runInDifferentProjectPublicId.id)
                get { createdTimestamp }.isEqualTo(currentCoverageTestRunDB.createdTimestamp.toInstant(ZoneOffset.UTC))
                get { coveredPercentage }.isEqualTo(JacocoXmlLoader.junitResultsParserLineCoveragePercentage)
                get { repo }.isEqualTo(repoName)
            }
        }

    @Test
    fun `should fetch current coverage for repository with branch name`() =
        testSuspend {
            val repoName = randomFullRepoName()

            val firstRunPublicId = randomPublicId()
            val secondRunPublicId = randomPublicId()
            val thirdRunPublicId = randomPublicId()

            val runInDifferentProjectPublicId = randomPublicId()
            val runInDifferentRepoPublicId = randomPublicId()

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = firstRunPublicId,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repoName,
                branchName = "my-branch",
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

            val response = testClient.get("/api/v1/repo/$repoName/coverage/current?branch=my-branch")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            val responseBody = response.bodyAsText()
            assertNotNull(responseBody)

            val currentCoverage: RepositoryCurrentCoverage = objectMapper.readValue(responseBody)

            val currentCoverageTestRunDB = testRunDao.fetchOneByPublicId(firstRunPublicId.id)

            expectThat(currentCoverage) {
                get { id }.isEqualTo(firstRunPublicId.id)
                get { createdTimestamp }.isEqualTo(currentCoverageTestRunDB.createdTimestamp.toInstant(ZoneOffset.UTC))
                get { coveredPercentage }.isEqualTo(JacocoXmlLoader.serverAppReducedLineCoveragePercentage)
                get { repo }.isEqualTo(repoName)
                get { branch }.isEqualTo("my-branch")
            }
        }

    @Test
    fun `when no branch specific should fetch current coverage from mainline branch`() =
        testSuspend {
            val repoName = randomFullRepoName()

            val otherBranchRunPublicId1 = randomPublicId()
            val mainRunPublicId = randomPublicId()
            val otherBranchRunPublicId2 = randomPublicId()

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = otherBranchRunPublicId1,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repoName,
                branchName = "other",
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = mainRunPublicId,
                coverageText = JacocoXmlLoader().serverApp(),
                repoName = repoName,
                branchName = "main",
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = otherBranchRunPublicId2,
                coverageText = JacocoXmlLoader().jacocoXmlParser(),
                repoName = repoName,
                branchName = "other",
            )

            val response = testClient.get("/api/v1/repo/$repoName/coverage/current")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            val responseBody = response.bodyAsText()
            assertNotNull(responseBody)

            val currentCoverage: RepositoryCurrentCoverage = objectMapper.readValue(responseBody)

            val currentCoverageTestRunDB = testRunDao.fetchOneByPublicId(mainRunPublicId.id)

            expectThat(currentCoverage) {
                get { id }.isEqualTo(mainRunPublicId.id)
                get { createdTimestamp }.isEqualTo(currentCoverageTestRunDB.createdTimestamp.toInstant(ZoneOffset.UTC))
                get { coveredPercentage }.isEqualTo(JacocoXmlLoader.serverAppLineCoveragePercentage)
                get { repo }.isEqualTo(repoName)
                get { branch }.isEqualTo("main")
            }
        }
}
