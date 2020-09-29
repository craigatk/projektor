package projektor.coverage

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.coverage.CoverageExists
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import kotlin.test.assertNotNull

@KtorExperimentalAPI
@ExperimentalStdlibApi
class CoverageExistsApplicationTest : ApplicationTestCase() {

    @Test
    fun `when report has coverage data should return true`() {
        val publicId = randomPublicId()

        val reportXmlBytes = JacocoXmlLoader().serverApp().toByteArray()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/coverage") {
                testRunDBGenerator.createSimpleTestRun(publicId)

                setBody(reportXmlBytes)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
                expectThat(coverageRuns).hasSize(1)
            }

            handleRequest(HttpMethod.Get, "/run/$publicId/coverage/exists").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverageExists = objectMapper.readValue(response.content, CoverageExists::class.java)
                assertNotNull(coverageExists)

                expectThat(coverageExists.exists).isTrue()
            }
        }
    }

    @Test
    fun `when report does not have coverage data should return false`() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/coverage/exists") {
                testRunDBGenerator.createSimpleTestRun(publicId)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverageExists = objectMapper.readValue(response.content, CoverageExists::class.java)
                assertNotNull(coverageExists)

                expectThat(coverageExists.exists).isFalse()
            }
        }
    }
}
