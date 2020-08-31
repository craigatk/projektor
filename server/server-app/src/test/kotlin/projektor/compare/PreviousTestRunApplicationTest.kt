package projektor.compare

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import kotlin.test.assertNotNull
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.PublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@KtorExperimentalAPI
@ExperimentalStdlibApi
class PreviousTestRunApplicationTest : ApplicationTestCase() {
    @Test
    fun `should find previous test run`() {
        val differentRepoPublicId = randomPublicId()
        val oldestPublicId = randomPublicId()
        val newerPublicId = randomPublicId()
        val thisPublicId = randomPublicId()

        val repoName = "${RandomStringUtils.randomAlphabetic(8)}/${RandomStringUtils.randomAlphabetic(8)}"

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$thisPublicId/previous") {
                val differentTestRun = testRunDBGenerator.createSimpleTestRun(differentRepoPublicId)
                testRunDBGenerator.addGitMetadata(differentTestRun, "projektor/different", true, "main")

                val oldestTestRun = testRunDBGenerator.createSimpleTestRun(oldestPublicId)
                testRunDBGenerator.addGitMetadata(oldestTestRun, repoName, true, "main")

                val newerTestRun = testRunDBGenerator.createSimpleTestRun(newerPublicId)
                testRunDBGenerator.addGitMetadata(newerTestRun, repoName, true, "main")

                val thisPublicTestRun = testRunDBGenerator.createSimpleTestRun(thisPublicId)
                testRunDBGenerator.addGitMetadata(thisPublicTestRun, repoName, true, "main")
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val previousId = objectMapper.readValue(response.content, PublicId::class.java)
                assertNotNull(previousId)

                expectThat(previousId).isEqualTo(oldestPublicId)
            }
        }
    }

    @Test
    fun `when no previous test run in same repo should return 204`() {
        val publicId = randomPublicId()
        val repoName = "${RandomStringUtils.randomAlphabetic(8)}/${RandomStringUtils.randomAlphabetic(8)}"

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/previous") {
                val differentTestRun = testRunDBGenerator.createSimpleTestRun(publicId)
                testRunDBGenerator.addGitMetadata(differentTestRun, repoName, true, "main")
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
            }
        }
    }
}
