package projektor.coverage

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.error.FailureBodyType
import projektor.incomingresults.randomPublicId
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

@KtorExperimentalAPI
class AddCoverageFailureApplicationTest : ApplicationTestCase() {
    @Test
    fun `when adding coverage fails should record failure`() {
        val publicId = randomPublicId()

        val invalidReportXml = JacocoXmlLoader().serverAppInvalid()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/coverage") {
                testRunDBGenerator.createSimpleTestRun(publicId)

                setBody(invalidReportXml.toByteArray())
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)

                val processingFailures = processingFailureDao.fetchByPublicId(publicId.id)
                expectThat(processingFailures).hasSize(1)

                val processingFailure = processingFailures[0]

                expectThat(processingFailure) {
                    get { body }.isEqualTo(invalidReportXml)
                    get { bodyType }.isEqualTo(FailureBodyType.COVERAGE.name)
                    get { failure }.contains("Unexpected EOF; was expecting a close tag for element <report>")
                }
            }
        }
    }
}
