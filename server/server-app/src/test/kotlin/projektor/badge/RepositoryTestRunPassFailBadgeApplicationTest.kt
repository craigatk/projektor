package projektor.badge

import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.util.randomOrgAndRepo
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class RepositoryTestRunPassFailBadgeApplicationTest : ApplicationTestCase() {
    @Test
    fun `when latest build passing without project should create badge`() {
        val repoName = randomOrgAndRepo()

        val mainlinePublicId = randomPublicId()
        val featureBranchPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/badge/tests") {
                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = mainlinePublicId,
                    coverageText = JacocoXmlLoader().serverAppReduced(),
                    repoName = repoName,
                    branchName = "main"
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = featureBranchPublicId,
                    coverageText = JacocoXmlLoader().serverApp(),
                    repoName = repoName,
                    branchName = "feature/dev"
                )
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
                expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

                expectThat(response.content).isNotNull().contains("passing")
            }
        }
    }

    @Test
    fun `when latest build failing without project should create badge`() {
        val repoName = randomOrgAndRepo()

        val passingPublicId = randomPublicId()
        val failingPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/badge/tests") {
                testRunDBGenerator.createSimpleTestRunInRepo(
                    publicId = passingPublicId,
                    repoName = repoName,
                    ci = true,
                    projectName = null
                )

                testRunDBGenerator.createSimpleFailingTestRunInRepo(
                    publicId = failingPublicId,
                    repoName = repoName,
                    ci = true,
                    projectName = null
                )
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
                expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

                expectThat(response.content).isNotNull().contains("failing")
            }
        }
    }

    @Test
    fun `when latest build passing with project should create badge`() {
        val repoName = randomOrgAndRepo()

        val mainlinePublicId = randomPublicId()
        val featureBranchPublicId = randomPublicId()
        val projectName = "project-name"

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/project/$projectName/badge/tests") {
                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = mainlinePublicId,
                    coverageText = JacocoXmlLoader().serverAppReduced(),
                    repoName = repoName,
                    branchName = "main",
                    projectName = projectName
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = featureBranchPublicId,
                    coverageText = JacocoXmlLoader().serverApp(),
                    repoName = repoName,
                    branchName = "feature/dev",
                    projectName = projectName
                )
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
                expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

                expectThat(response.content).isNotNull().contains("passing")
            }
        }
    }
}
