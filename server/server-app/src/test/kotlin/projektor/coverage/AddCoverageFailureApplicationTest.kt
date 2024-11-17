package projektor.coverage

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
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
    fun `when adding coverage fails should record failure`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            val invalidReportXml = JacocoXmlLoader().serverAppInvalid()

            testRunDBGenerator.createSimpleTestRun(publicId)

            val postResponse =
                client.post("/run/$publicId/coverage") {
                    setBody(invalidReportXml.toByteArray())
                }

            expectThat(postResponse.status).isEqualTo(HttpStatusCode.BadRequest)

            val saveCoverageErrorResponse = objectMapper.readValue(postResponse.bodyAsText(), SaveCoverageError::class.java)

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

    @Test
    fun `when adding malformed Jacoco report that is empty should ignore it`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            val emptyReportXml = JacocoXmlLoader().emptyReport()

            testRunDBGenerator.createSimpleTestRun(publicId)

            val postResponse =
                client.post("/run/$publicId/coverage") {
                    setBody(emptyReportXml.toByteArray())
                }

            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            expectThat(meterRegistry.counter("coverage_parse_failure").count()).isEqualTo(0.toDouble())
            expectThat(meterRegistry.counter("coverage_process_failure").count()).isEqualTo(0.toDouble())
        }
}
