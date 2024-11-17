package projektor.badge

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.util.randomOrgAndRepo
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

class RepositoryTestRunPassFailBadgeApplicationTest : ApplicationTestCase() {
    @Test
    fun `when latest build passing without project should create badge`() =
        projektorTestApplication {
            val repoName = randomOrgAndRepo()

            val mainlinePublicId = randomPublicId()
            val featureBranchPublicId = randomPublicId()

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = mainlinePublicId,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repoName,
                branchName = "main",
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = featureBranchPublicId,
                coverageText = JacocoXmlLoader().serverApp(),
                repoName = repoName,
                branchName = "feature/dev",
            )

            val response = client.get("/repo/$repoName/badge/tests")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

            expectThat(response.bodyAsText()).contains("passing")
        }

    @Test
    fun `when latest build failing without project should create badge`() =
        projektorTestApplication {
            val repoName = randomOrgAndRepo()

            val passingPublicId = randomPublicId()
            val failingPublicId = randomPublicId()

            testRunDBGenerator.createSimpleTestRunInRepo(
                publicId = passingPublicId,
                repoName = repoName,
                ci = true,
                projectName = null,
            )

            testRunDBGenerator.createSimpleFailingTestRunInRepo(
                publicId = failingPublicId,
                repoName = repoName,
                ci = true,
                projectName = null,
            )

            val response = client.get("/repo/$repoName/badge/tests")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

            expectThat(response.bodyAsText()).contains("failing")
        }

    @Test
    fun `when latest build passing with project should create badge`() =
        projektorTestApplication {
            val repoName = randomOrgAndRepo()

            val mainlinePublicId = randomPublicId()
            val featureBranchPublicId = randomPublicId()
            val projectName = "project-name"

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = mainlinePublicId,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repoName,
                branchName = "main",
                projectName = projectName,
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = featureBranchPublicId,
                coverageText = JacocoXmlLoader().serverApp(),
                repoName = repoName,
                branchName = "feature/dev",
                projectName = projectName,
            )

            val response = client.get("/repo/$repoName/project/$projectName/badge/tests")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

            expectThat(response.bodyAsText()).contains("passing")
        }
}
