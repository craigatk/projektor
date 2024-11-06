package projektor.badge

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.util.randomOrgAndRepo
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

class TestRunCoverageBadgeApplicationTest : ApplicationTestCase() {
    override fun autoStartServer() = true

    @Test
    fun `should create badge for test run coverage percentage`() =
        testSuspend {
            val publicId = randomPublicId()
            val repoName = randomOrgAndRepo()

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = publicId,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repoName,
            )

            val response = testClient.get("/run/$publicId/badge/coverage")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

            expectThat(response.bodyAsText()).contains("95%")
        }

    @Test
    fun `should return 404 when test run doesn't have coverage data`() =
        testSuspend {
            val publicId = randomPublicId()

            testRunDBGenerator.createSimpleTestRun(publicId)

            val response = testClient.get("/run/$publicId/badge/coverage")

            expectThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        }
}
