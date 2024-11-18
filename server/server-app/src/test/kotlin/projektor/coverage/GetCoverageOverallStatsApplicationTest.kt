package projektor.coverage

import io.ktor.client.request.*
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class GetCoverageOverallStatsApplicationTest : ApplicationTestCase() {
    @Test
    fun `when test run has no coverage data should return 204`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            testRunDBGenerator.createSimpleTestRun(publicId)

            val response = client.get("/run/$publicId/coverage/overall")

            expectThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        }
}
