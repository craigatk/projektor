package projektor.badge

import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.util.randomOrgAndRepo
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class TestRunTestsBadgeApplicationTest : ApplicationTestCase() {
    @Test
    fun `when test run passed should tests create badge`() {
        val repoName = randomOrgAndRepo()

        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/badge/tests") {
                testRunDBGenerator.createSimpleTestRunInRepo(publicId, repoName, true, null)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
                expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

                expectThat(response.content).isNotNull().contains("passing")
            }
        }
    }

    @Test
    fun `when test run failed should tests create badge`() {
        val repoName = randomOrgAndRepo()

        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/badge/tests") {
                testRunDBGenerator.createSimpleFailingTestRunInRepo(publicId, repoName, true, null)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
                expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

                expectThat(response.content).isNotNull().contains("failing")
            }
        }
    }
}
