package projektor.compare

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.PublicId
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

@KtorExperimentalAPI
@ExperimentalStdlibApi
class PreviousTestRunApplicationTest : ApplicationTestCase() {
    @Test
    fun `should find previous test run`() {
        val differentRepoPublicId = randomPublicId()
        val oldestPublicId = randomPublicId()
        val previousPublicId = randomPublicId()
        val thisPublicId = randomPublicId()

        val repoName = "${RandomStringUtils.randomAlphabetic(8)}/${RandomStringUtils.randomAlphabetic(8)}"

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$thisPublicId/previous") {
                val differentTestRun = testRunDBGenerator.createSimpleTestRun(differentRepoPublicId)
                testRunDBGenerator.addGitMetadata(differentTestRun, "projektor/different", true, "main", null)
                runBlocking { coverageService.saveReport(JacocoXmlLoader().serverApp(), differentRepoPublicId) }

                val oldestTestRun = testRunDBGenerator.createSimpleTestRun(oldestPublicId)
                testRunDBGenerator.addGitMetadata(oldestTestRun, repoName, true, "main", null)
                runBlocking { coverageService.saveReport(JacocoXmlLoader().serverApp(), oldestPublicId) }

                val newerTestRun = testRunDBGenerator.createSimpleTestRun(previousPublicId)
                testRunDBGenerator.addGitMetadata(newerTestRun, repoName, true, "main", null)
                runBlocking { coverageService.saveReport(JacocoXmlLoader().serverApp(), previousPublicId) }

                val thisPublicTestRun = testRunDBGenerator.createSimpleTestRun(thisPublicId)
                testRunDBGenerator.addGitMetadata(thisPublicTestRun, repoName, true, "main", null)
                runBlocking { coverageService.saveReport(JacocoXmlLoader().serverApp(), thisPublicId) }
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val previousId = objectMapper.readValue(response.content, PublicId::class.java)
                assertNotNull(previousId)

                expectThat(previousId).isEqualTo(previousPublicId)
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
                testRunDBGenerator.addGitMetadata(differentTestRun, repoName, true, "main", null)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
            }
        }
    }
}
