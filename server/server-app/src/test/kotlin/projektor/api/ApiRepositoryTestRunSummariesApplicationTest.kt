package projektor.api

import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.createTestRun
import projektor.incomingresults.randomPublicId
import projektor.server.api.repository.RepositoryTestRunSummaries
import projektor.util.randomFullRepoName
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import kotlin.test.assertNotNull

class ApiRepositoryTestRunSummariesApplicationTest : ApplicationTestCase() {
    @Test
    fun `when no project should get test run summaries for repo`() {
        val repoName = randomFullRepoName()

        val firstRunPublicId = randomPublicId()
        val secondRunPublicId = randomPublicId()
        val thirdRunPublicId = randomPublicId()

        val otherBranchRunPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/api/v1/repo/$repoName/tests/runs/summaries") {
                val firstTestRun = createTestRun(firstRunPublicId, 20, 0, BigDecimal("10.001"))
                testRunDao.insert(firstTestRun)
                testRunDBGenerator.addGitMetadata(firstTestRun, repoName, true, "main", null, null, null)
                testRunDBGenerator.addResultsMetadata(firstTestRun, true)

                val secondTestRun = createTestRun(secondRunPublicId, 30, 0, BigDecimal("15.001"))
                testRunDao.insert(secondTestRun)
                testRunDBGenerator.addGitMetadata(secondTestRun, repoName, true, "main", null, null, null)
                testRunDBGenerator.addResultsMetadata(secondTestRun, true)

                val thirdTestRun = createTestRun(thirdRunPublicId, 45, 0, BigDecimal("25.001"))
                testRunDao.insert(thirdTestRun)
                testRunDBGenerator.addGitMetadata(thirdTestRun, repoName, true, "main", null, null, null)
                testRunDBGenerator.addResultsMetadata(thirdTestRun, true)

                val otherBranchRunPublicId = createTestRun(otherBranchRunPublicId, 50, 0, BigDecimal("30.001"))
                testRunDao.insert(otherBranchRunPublicId)
                testRunDBGenerator.addGitMetadata(otherBranchRunPublicId, repoName, false, "feature-branch", null, null, null)
                testRunDBGenerator.addResultsMetadata(otherBranchRunPublicId, true)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val repositoryTestRunSummaries = objectMapper.readValue(response.content, RepositoryTestRunSummaries::class.java)
                assertNotNull(repositoryTestRunSummaries)

                expectThat(repositoryTestRunSummaries.testRuns).hasSize(3)

                // should sort in descending order of date created
                expectThat(repositoryTestRunSummaries.testRuns.map { it.id }[0]).isEqualTo(thirdRunPublicId.id)
                expectThat(repositoryTestRunSummaries.testRuns.map { it.id }[1]).isEqualTo(secondRunPublicId.id)
                expectThat(repositoryTestRunSummaries.testRuns.map { it.id }[2]).isEqualTo(firstRunPublicId.id)
            }
        }
    }

    @Test
    fun `when no project and limit should get test run summaries for repo within limit`() {
        val repoName = randomFullRepoName()

        val firstRunPublicId = randomPublicId()
        val secondRunPublicId = randomPublicId()
        val thirdRunPublicId = randomPublicId()

        val otherBranchRunPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/api/v1/repo/$repoName/tests/runs/summaries?limit=2") {
                val firstTestRun = createTestRun(firstRunPublicId, 20, 0, BigDecimal("10.001"))
                testRunDao.insert(firstTestRun)
                testRunDBGenerator.addGitMetadata(firstTestRun, repoName, true, "main", null, null, null)
                testRunDBGenerator.addResultsMetadata(firstTestRun, true)

                val secondTestRun = createTestRun(secondRunPublicId, 30, 0, BigDecimal("15.001"))
                testRunDao.insert(secondTestRun)
                testRunDBGenerator.addGitMetadata(secondTestRun, repoName, true, "main", null, null, null)
                testRunDBGenerator.addResultsMetadata(secondTestRun, true)

                val thirdTestRun = createTestRun(thirdRunPublicId, 45, 0, BigDecimal("25.001"))
                testRunDao.insert(thirdTestRun)
                testRunDBGenerator.addGitMetadata(thirdTestRun, repoName, true, "main", null, null, null)
                testRunDBGenerator.addResultsMetadata(thirdTestRun, true)

                val otherBranchRunPublicId = createTestRun(otherBranchRunPublicId, 50, 0, BigDecimal("30.001"))
                testRunDao.insert(otherBranchRunPublicId)
                testRunDBGenerator.addGitMetadata(otherBranchRunPublicId, repoName, false, "feature-branch", null, null, null)
                testRunDBGenerator.addResultsMetadata(otherBranchRunPublicId, true)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val repositoryTestRunSummaries = objectMapper.readValue(response.content, RepositoryTestRunSummaries::class.java)
                assertNotNull(repositoryTestRunSummaries)

                expectThat(repositoryTestRunSummaries.testRuns).hasSize(2)

                // should sort in descending order of date created
                expectThat(repositoryTestRunSummaries.testRuns.map { it.id }[0]).isEqualTo(thirdRunPublicId.id)
                expectThat(repositoryTestRunSummaries.testRuns.map { it.id }[1]).isEqualTo(secondRunPublicId.id)
            }
        }
    }

    @Test
    fun `when project name should get test run summaries for repo and project`() {
        val repoName = randomFullRepoName()
        val projectName = "my-proj"

        val firstRunPublicId = randomPublicId()
        val secondRunPublicId = randomPublicId()
        val thirdRunPublicId = randomPublicId()

        val otherBranchRunPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/api/v1/repo/$repoName/tests/runs/summaries?project=$projectName") {
                val firstTestRun = createTestRun(firstRunPublicId, 20, 0, BigDecimal("10.001"))
                testRunDao.insert(firstTestRun)
                testRunDBGenerator.addGitMetadata(firstTestRun, repoName, true, "main", projectName, null, null)
                testRunDBGenerator.addResultsMetadata(firstTestRun, true)

                val secondTestRun = createTestRun(secondRunPublicId, 30, 0, BigDecimal("15.001"))
                testRunDao.insert(secondTestRun)
                testRunDBGenerator.addGitMetadata(secondTestRun, repoName, true, "main", projectName, null, null)
                testRunDBGenerator.addResultsMetadata(secondTestRun, true)

                val thirdTestRun = createTestRun(thirdRunPublicId, 45, 0, BigDecimal("25.001"))
                testRunDao.insert(thirdTestRun)
                testRunDBGenerator.addGitMetadata(thirdTestRun, repoName, true, "main", projectName, null, null)
                testRunDBGenerator.addResultsMetadata(thirdTestRun, true)

                val otherBranchRunPublicId = createTestRun(otherBranchRunPublicId, 50, 0, BigDecimal("30.001"))
                testRunDao.insert(otherBranchRunPublicId)
                testRunDBGenerator.addGitMetadata(otherBranchRunPublicId, repoName, false, "feature-branch", projectName, null, null)
                testRunDBGenerator.addResultsMetadata(otherBranchRunPublicId, true)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val repositoryTestRunSummaries = objectMapper.readValue(response.content, RepositoryTestRunSummaries::class.java)
                assertNotNull(repositoryTestRunSummaries)

                expectThat(repositoryTestRunSummaries.testRuns).hasSize(3)

                // should sort in descending order of date created
                expectThat(repositoryTestRunSummaries.testRuns.map { it.id }[0]).isEqualTo(thirdRunPublicId.id)
                expectThat(repositoryTestRunSummaries.testRuns.map { it.id }[1]).isEqualTo(secondRunPublicId.id)
                expectThat(repositoryTestRunSummaries.testRuns.map { it.id }[2]).isEqualTo(firstRunPublicId.id)
            }
        }
    }
}
