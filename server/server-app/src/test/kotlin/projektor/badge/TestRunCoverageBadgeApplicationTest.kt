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
class TestRunCoverageBadgeApplicationTest : ApplicationTestCase() {
    @Test
    fun `should create badge for test run coverage percentage`() {
        val publicId = randomPublicId()
        val repoName = randomOrgAndRepo()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/badge/coverage") {
                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = publicId,
                    coverageText = JacocoXmlLoader().serverAppReduced(),
                    repoName = repoName
                )
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
                expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

                expectThat(response.content).isNotNull().contains("95%")
            }
        }
    }

    @Test
    fun `should return 404 when test run doesn't have coverage data`() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/badge/coverage") {
                testRunDBGenerator.createSimpleTestRun(publicId)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }
        }
    }
}
