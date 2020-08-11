package projektor.coverage

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

@KtorExperimentalAPI
@ExperimentalStdlibApi
class AddCoverageApplicationTest : ApplicationTestCase() {
    @Test
    fun `should add Jacoco coverage to test run then get it`() {
        val publicId = randomPublicId()

        val reportXmlBytes = JacocoXmlLoader().serverApp().toByteArray()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/coverage/jacoco") {
                testRunDBGenerator.createTestRun(
                        publicId,
                        listOf(
                                TestSuiteData("testSuite1",
                                        listOf("testSuite1TestCase1"),
                                        listOf(),
                                        listOf()
                                )
                        )
                )

                setBody(reportXmlBytes)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
                expectThat(coverageRuns).hasSize(1)
            }
        }
    }
}