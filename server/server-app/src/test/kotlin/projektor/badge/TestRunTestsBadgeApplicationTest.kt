package projektor.badge

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.util.randomOrgAndRepo
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

class TestRunTestsBadgeApplicationTest : ApplicationTestCase() {
    @Test
    fun `when test run passed should tests create badge`() =
        projektorTestApplication {
            val repoName = randomOrgAndRepo()

            val publicId = randomPublicId()

            testRunDBGenerator.createSimpleTestRunInRepo(publicId, repoName, true, null)

            val response = client.get("/run/$publicId/badge/tests")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

            expectThat(response.bodyAsText()).contains("passing")
        }

    @Test
    fun `when test run failed should tests create badge`() =
        projektorTestApplication {
            val repoName = randomOrgAndRepo()

            val publicId = randomPublicId()

            testRunDBGenerator.createSimpleFailingTestRunInRepo(publicId, repoName, true, null)

            val response = client.get("/run/$publicId/badge/tests")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)
            expectThat(response.contentType().toString()).contains(ContentType.Image.SVG.toString())

            expectThat(response.bodyAsText()).contains("failing")
        }
}
