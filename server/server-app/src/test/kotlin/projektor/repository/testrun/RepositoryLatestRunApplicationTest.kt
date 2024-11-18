package projektor.repository.testrun

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class RepositoryLatestRunApplicationTest : ApplicationTestCase() {
    @Test
    fun `when repo with no project has test run should redirect to latest test run`() =
        projektorTestApplication {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "$orgName/repo"
            val projectName = null

            val olderRunPublicId = randomPublicId()
            val newerRunPublicId = randomPublicId()

            testRunDBGenerator.createSimpleTestRunInRepo(olderRunPublicId, repoName, true, projectName)
            testRunDBGenerator.createSimpleTestRunInRepo(newerRunPublicId, repoName, true, projectName)

            val response = client.get("/repo/$repoName/run/latest")

            expectThat(response.request.url.encodedPath).isEqualTo("/tests/$newerRunPublicId")
        }

    @Test
    fun `when repo with with project has test run should redirect to latest test run`() =
        projektorTestApplication {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "$orgName/repo"
            val projectName = "my-proj"

            val olderRunPublicId = randomPublicId()
            val newerRunPublicId = randomPublicId()

            testRunDBGenerator.createSimpleTestRunInRepo(olderRunPublicId, repoName, true, projectName)
            testRunDBGenerator.createSimpleTestRunInRepo(newerRunPublicId, repoName, true, projectName)

            val response = client.get("/repo/$repoName/project/$projectName/run/latest")

            expectThat(response.request.url.encodedPath).isEqualTo("/tests/$newerRunPublicId")
        }

    @Test
    fun `when repo with no project has no test runs should return no-content response`() =
        projektorTestApplication {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "$orgName/repo"

            val response = client.get("/repo/$repoName/run/latest")

            expectThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        }

    @Test
    fun `when repo with project has no test runs should return no-content response`() =
        projektorTestApplication {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "$orgName/repo"
            val projectName = "my-proj"

            val response = client.get("/repo/$repoName/project/$projectName/run/latest")

            expectThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        }
}
