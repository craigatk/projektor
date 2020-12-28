package projektor.badge

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.util.randomOrgAndRepo
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@KtorExperimentalAPI
class RepositoryCoverageBadgeApplicationTest : ApplicationTestCase() {
    @Test
    fun `should create coverage badge for repository`() {
        val repoName = randomOrgAndRepo()

        val thisPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/badge/coverage") {
                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = thisPublicId,
                    coverageText = JacocoXmlLoader().serverAppReduced(),
                    repoName = repoName
                )
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
                expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

                expectThat(response.content).isNotNull().contains("95.40%")
            }
        }
    }

    @Test
    fun `when no test runs with coverage should return 404`() {
        val repoName = randomOrgAndRepo()

        val thisPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/badge/coverage") {
                testRunDBGenerator.createEmptyTestRunInRepo(
                    publicId = thisPublicId,
                    repoName = repoName,
                    ci = true,
                    projectName = null
                )
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }
        }
    }

    @Test
    fun `should create coverage badge for repository with project`() {
        val repoName = randomOrgAndRepo()
        val projectName = "my-project"

        val thisPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/project/$projectName/badge/coverage") {
                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = thisPublicId,
                    coverageText = JacocoXmlLoader().serverAppReduced(),
                    repoName = repoName,
                    branchName = "main",
                    projectName = projectName
                )
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
                expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

                expectThat(response.content).isNotNull().contains("95.40%")
            }
        }
    }

    @Test
    fun `when no test runs with coverage in project should return 404`() {
        val repoName = randomOrgAndRepo()
        val projectName = "my-project"

        val thisPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/project/$projectName/badge/coverage") {
                testRunDBGenerator.createEmptyTestRunInRepo(
                    publicId = thisPublicId,
                    repoName = repoName,
                    ci = true,
                    projectName = projectName
                )
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }
        }
    }
}
