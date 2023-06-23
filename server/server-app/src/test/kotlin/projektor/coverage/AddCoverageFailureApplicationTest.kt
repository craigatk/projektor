package projektor.coverage

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.coverage.SaveCoverageError
import projektor.server.api.error.FailureBodyType
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

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

                val saveCoverageErrorResponse = objectMapper.readValue(response.content, SaveCoverageError::class.java)

                expectThat(saveCoverageErrorResponse) {
                    get { id }.isEqualTo(publicId.id)
                    get { errorMessage }.isNotNull().contains("Unexpected EOF; was expecting a close tag for element <report>")
                }

                val processingFailures = resultsProcessingFailureDao.fetchByPublicId(publicId.id)
                expectThat(processingFailures).hasSize(1)

                val processingFailure = processingFailures[0]

                expectThat(processingFailure) {
                    get { body }.isEqualTo(invalidReportXml)
                    get { bodyType }.isEqualTo(FailureBodyType.COVERAGE.name)
                    get { failureMessage }.contains("Unexpected EOF; was expecting a close tag for element <report>")
                }

                expectThat(meterRegistry.counter("coverage_parse_failure").count()).isEqualTo(1.toDouble())
                expectThat(meterRegistry.counter("coverage_process_failure").count()).isEqualTo(0.toDouble())
            }
        }
    }

    @Test
    fun `when adding malformed Jacoco report that is empty should ignore it`() {
        val publicId = randomPublicId()

        val emptyReportXml = JacocoXmlLoader().emptyReport()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/coverage") {
                testRunDBGenerator.createSimpleTestRun(publicId)

                setBody(emptyReportXml.toByteArray())
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                expectThat(meterRegistry.counter("coverage_parse_failure").count()).isEqualTo(0.toDouble())
                expectThat(meterRegistry.counter("coverage_process_failure").count()).isEqualTo(0.toDouble())
            }
        }
    }
}
