package projektor.repository.testrun

import io.ktor.http.*
import io.ktor.server.testing.*
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class RepositoryLatestRunApplicationTest : ApplicationTestCase() {
    @Test
    fun `when repo with no project has test run should redirect to latest test run`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = null

        val olderRunPublicId = randomPublicId()
        val newerRunPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/run/latest") {
                testRunDBGenerator.createSimpleTestRunInRepo(olderRunPublicId, repoName, true, projectName)
                testRunDBGenerator.createSimpleTestRunInRepo(newerRunPublicId, repoName, true, projectName)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.Found)

                expectThat(response.headers["Location"]).isEqualTo("/tests/$newerRunPublicId")
            }
        }
    }

    @Test
    fun `when repo with with project has test run should redirect to latest test run`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = "my-proj"

        val olderRunPublicId = randomPublicId()
        val newerRunPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/project/$projectName/run/latest") {
                testRunDBGenerator.createSimpleTestRunInRepo(olderRunPublicId, repoName, true, projectName)
                testRunDBGenerator.createSimpleTestRunInRepo(newerRunPublicId, repoName, true, projectName)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.Found)

                expectThat(response.headers["Location"]).isEqualTo("/tests/$newerRunPublicId")
            }
        }
    }

    @Test
    fun `when repo with no project has no test runs should return no-content response`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/run/latest") {
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
            }
        }
    }

    @Test
    fun `when repo with project has no test runs should return no-content response`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = "my-proj"

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/project/$projectName/run/latest") {
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
            }
        }
    }
}
