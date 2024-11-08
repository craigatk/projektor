package projektor.coverage

import io.ktor.client.request.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class GetCoverageOverallStatsApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `when test run has no coverage data should return 204`() =
        testSuspend {
            val publicId = randomPublicId()

            testRunDBGenerator.createSimpleTestRun(publicId)

            val response = testClient.get("/run/$publicId/coverage/overall")

            expectThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        }
}
