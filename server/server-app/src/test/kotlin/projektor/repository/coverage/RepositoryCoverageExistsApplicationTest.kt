package projektor.repository.coverage

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.testing.*
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.coverage.CoverageExists
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import kotlin.test.assertNotNull

class RepositoryCoverageExistsApplicationTest : ApplicationTestCase() {
    @Test
    fun `when repo has coverage should exist`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"

        val runInRepoPublicId = randomPublicId()
        val runInDifferentProjectPublicId = randomPublicId()
        val runInDifferentRepoPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/coverage/exists") {

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = runInRepoPublicId,
                    coverageText = JacocoXmlLoader().serverAppReduced(),
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
                val responseBody = response.content
                assertNotNull(responseBody)

                val coverageExists: CoverageExists = objectMapper.readValue(responseBody)
                expectThat(coverageExists.exists).isTrue()
            }
        }
    }

    @Test
    fun `when coverage exists only in project should not exist`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"

        val runInDifferentProjectPublicId = randomPublicId()
        val runInDifferentRepoPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/coverage/exists") {

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
                val responseBody = response.content
                assertNotNull(responseBody)

                val coverageExists: CoverageExists = objectMapper.readValue(responseBody)
                expectThat(coverageExists.exists).isFalse()
            }
        }
    }

    @Test
    fun `when repo and project has coverage should exist`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"

        val projectName = "myproject"

        val runInRepoPublicId = randomPublicId()
        val runInProjectPublicId = randomPublicId()
        val runInDifferentRepoPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/project/$projectName/coverage/exists") {

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = runInRepoPublicId,
                    coverageText = JacocoXmlLoader().serverAppReduced(),
                    repoName = repoName,
                    branchName = "main"
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = runInProjectPublicId,
                    coverageText = JacocoXmlLoader().junitResultsParser(),
                    repoName = repoName,
                    branchName = "main",
                    projectName = projectName
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = runInDifferentRepoPublicId,
                    coverageText = JacocoXmlLoader().junitResultsParser(),
                    repoName = "other/repo",
                    branchName = "main"
                )
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
                val responseBody = response.content
                assertNotNull(responseBody)

                val coverageExists: CoverageExists = objectMapper.readValue(responseBody)
                expectThat(coverageExists.exists).isTrue()
            }
        }
    }

    @Test
    fun `when coverage only in null project should not exist`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"

        val projectName = "myproject"

        val runInRepoPublicId = randomPublicId()
        val runInDifferentRepoPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/project/$projectName/coverage/exists") {

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = runInRepoPublicId,
                    coverageText = JacocoXmlLoader().serverAppReduced(),
                    repoName = repoName,
                    branchName = "main"
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = runInDifferentRepoPublicId,
                    coverageText = JacocoXmlLoader().junitResultsParser(),
                    repoName = "other/repo",
                    branchName = "main"
                )
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
                val responseBody = response.content
                assertNotNull(responseBody)

                val coverageExists: CoverageExists = objectMapper.readValue(responseBody)
                expectThat(coverageExists.exists).isFalse()
            }
        }
    }
}