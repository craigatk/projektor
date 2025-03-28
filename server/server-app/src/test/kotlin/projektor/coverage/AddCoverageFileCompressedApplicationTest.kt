package projektor.coverage

import io.ktor.client.request.*
import io.ktor.client.request.headers
import io.ktor.client.statement.*
import io.ktor.http.*
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
    fun `should save compressed coverage file with base directory path`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            val coverageGroup = "server-app"
            val coverageXmlReport = JacocoXmlLoader().serverApp()
            val coverageFilePayload =
                CoverageFilePayload(
                    reportContents = coverageXmlReport,
                    baseDirectoryPath = "server/server-app/src/main/kotlin",
                )
            val compressedBody = gzip(coveragePayloadParser.serializeCoverageFilePayload(coverageFilePayload))

            testRunDBGenerator.createSimpleTestRun(publicId)

            val postResponse =
                client.post("/run/$publicId/coverageFile") {
                    headers {
                        append(HttpHeaders.ContentType, "text/plain")
                        append(HttpHeaders.ContentEncoding, "gzip")
                    }
                    setBody(compressedBody)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
            expectThat(coverageRuns).hasSize(1)

            val getResponse = client.get("/run/$publicId/coverage/$coverageGroup/files")
            expectThat(getResponse.status).isEqualTo(HttpStatusCode.OK)

            val coverageFiles = objectMapper.readValue(getResponse.bodyAsText(), CoverageFiles::class.java)
            assertNotNull(coverageFiles)

            expectThat(coverageFiles.files).hasSize(62)

            val resultsProcessingDatabaseRepositoryFile = coverageFiles.files.find { it.fileName == "ResultsProcessingDatabaseRepository.kt" }

            expectThat(resultsProcessingDatabaseRepositoryFile)
                .isNotNull().and {
                    get { directoryName }.isEqualTo("incomingresults/processing")
                    get { filePath }.isEqualTo(
                        "server/server-app/src/main/kotlin/incomingresults/processing/ResultsProcessingDatabaseRepository.kt",
                    )
                }
        }
}
