package projektor.coverage

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.parser.coverage.payload.CoveragePayloadParser
import projektor.server.api.coverage.CoverageFiles
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.util.gzip
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import kotlin.test.assertNotNull

class AddCoverageFileCompressedApplicationTest : ApplicationTestCase() {
    private val coveragePayloadParser = CoveragePayloadParser()

    @Test
    fun `should save compressed coverage file with base directory path`() {
        val publicId = randomPublicId()

        val coverageGroup = "server-app"
        val coverageXmlReport = JacocoXmlLoader().serverApp()
        val coverageFilePayload = CoverageFilePayload(
            reportContents = coverageXmlReport,
            baseDirectoryPath = "server/server-app/src/main/kotlin"
        )
        val compressedBody = gzip(coveragePayloadParser.serializeCoverageFilePayload(coverageFilePayload))

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/coverageFile") {
                testRunDBGenerator.createSimpleTestRun(publicId)

                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.ContentEncoding, "gzip")
                setBody(compressedBody)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
                expectThat(coverageRuns).hasSize(1)
            }

            handleRequest(HttpMethod.Get, "/run/$publicId/coverage/$coverageGroup/files").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverageFiles = objectMapper.readValue(response.content, CoverageFiles::class.java)
                assertNotNull(coverageFiles)

                expectThat(coverageFiles.files).hasSize(62)

                val resultsProcessingDatabaseRepositoryFile = coverageFiles.files.find { it.fileName == "ResultsProcessingDatabaseRepository.kt" }

                expectThat(resultsProcessingDatabaseRepositoryFile)
                    .isNotNull().and {
                        get { directoryName }.isEqualTo("projektor/incomingresults/processing")
                        get { filePath }.isEqualTo("server/server-app/src/main/kotlin/projektor/incomingresults/processing/ResultsProcessingDatabaseRepository.kt")
                    }
            }
        }
    }
}
