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

class RepositoryCoverageBadgeApplicationTest : ApplicationTestCase() {
    @Test
    fun `should create coverage badge for repository mainline branch by default`() =
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

            val response = client.get("/repo/$repoName/badge/coverage")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

            expectThat(response.bodyAsText()).contains("95%")
        }

    @Test
    fun `when no coverage in mainline should create coverage badge for repository with data from all branches`() =
        projektorTestApplication {
            val repoName = randomOrgAndRepo()

            val featureBranchPublicId = randomPublicId()

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = featureBranchPublicId,
                coverageText = JacocoXmlLoader().serverApp(),
                repoName = repoName,
                branchName = "feature/dev",
            )

            val response = client.get("/repo/$repoName/badge/coverage")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

            expectThat(response.bodyAsText()).contains("97%")
        }

    @Test
    fun `when no test runs with coverage should return 404`() =
        projektorTestApplication {
            val repoName = randomOrgAndRepo()

            val thisPublicId = randomPublicId()

            testRunDBGenerator.createEmptyTestRunInRepo(
                publicId = thisPublicId,
                repoName = repoName,
                ci = true,
                projectName = null,
            )

            val response = client.get("/repo/$repoName/badge/coverage")

            expectThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        }

    @Test
    fun `should create coverage badge for repository with project`() =
        projektorTestApplication {
            val repoName = randomOrgAndRepo()
            val projectName = "my-project"

            val thisPublicId = randomPublicId()

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = thisPublicId,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repoName,
                branchName = "main",
                projectName = projectName,
            )

            val response = client.get("/repo/$repoName/project/$projectName/badge/coverage")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

            expectThat(response.bodyAsText()).contains("95%")
        }

    @Test
    fun `when no test runs with coverage in project should return 404`() =
        projektorTestApplication {
            val repoName = randomOrgAndRepo()
            val projectName = "my-project"

            val thisPublicId = randomPublicId()

            testRunDBGenerator.createEmptyTestRunInRepo(
                publicId = thisPublicId,
                repoName = repoName,
                ci = true,
                projectName = projectName,
            )

            val response = client.get("/repo/$repoName/project/$projectName/badge/coverage")

            expectThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        }
}
