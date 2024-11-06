package projektor.compare

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.server.api.PublicId
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

class PreviousTestRunApplicationTest : ApplicationTestCase() {
    override fun autoStartServer() = true

    @Test
    fun `should find previous test run`() =
        testSuspend {
            val differentRepoPublicId = randomPublicId()
            val oldestPublicId = randomPublicId()
            val previousPublicId = randomPublicId()
            val thisPublicId = randomPublicId()

            val repoName = "${RandomStringUtils.randomAlphabetic(8)}/${RandomStringUtils.randomAlphabetic(8)}"

            val differentTestRun = testRunDBGenerator.createSimpleTestRun(differentRepoPublicId)
            testRunDBGenerator.addGitMetadata(differentTestRun, "projektor/different", true, "main", null, null, null)
            runBlocking { coverageService.saveReport(CoverageFilePayload(JacocoXmlLoader().serverApp()), differentRepoPublicId) }

            val oldestTestRun = testRunDBGenerator.createSimpleTestRun(oldestPublicId)
            testRunDBGenerator.addGitMetadata(oldestTestRun, repoName, true, "main", null, null, null)
            runBlocking { coverageService.saveReport(CoverageFilePayload(JacocoXmlLoader().serverApp()), oldestPublicId) }

            val newerTestRun = testRunDBGenerator.createSimpleTestRun(previousPublicId)
            testRunDBGenerator.addGitMetadata(newerTestRun, repoName, true, "main", null, null, null)
            runBlocking { coverageService.saveReport(CoverageFilePayload(JacocoXmlLoader().serverApp()), previousPublicId) }

            val thisPublicTestRun = testRunDBGenerator.createSimpleTestRun(thisPublicId)
            testRunDBGenerator.addGitMetadata(thisPublicTestRun, repoName, true, "main", null, null, null)
            runBlocking { coverageService.saveReport(CoverageFilePayload(JacocoXmlLoader().serverApp()), thisPublicId) }

            val response = testClient.get("/run/$thisPublicId/previous")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val previousId = objectMapper.readValue(response.bodyAsText(), PublicId::class.java)
            assertNotNull(previousId)

            expectThat(previousId).isEqualTo(previousPublicId)
        }

    @Test
    fun `when no previous test run in same repo should return 204`() =
        testSuspend {
            val publicId = randomPublicId()
            val repoName = "${RandomStringUtils.randomAlphabetic(8)}/${RandomStringUtils.randomAlphabetic(8)}"

            val differentTestRun = testRunDBGenerator.createSimpleTestRun(publicId)
            testRunDBGenerator.addGitMetadata(differentTestRun, repoName, true, "main", null, null, null)

            val response = testClient.get("/run/$publicId/previous")

            expectThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        }
}
