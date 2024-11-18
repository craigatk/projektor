package projektor.coverage

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
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

class CoverageExistsApplicationTest : ApplicationTestCase() {
    @Test
    fun `when report has coverage data should return true`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            val reportXmlBytes = JacocoXmlLoader().serverApp().toByteArray()

            testRunDBGenerator.createSimpleTestRun(publicId)

            val postResponse =
                client.post("/run/$publicId/coverage") {
                    setBody(reportXmlBytes)
                }
            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            val coverageRuns = coverageRunDao.fetchByTestRunPublicId(publicId.id)
            expectThat(coverageRuns).hasSize(1)

            val getResponse = client.get("/run/$publicId/coverage/exists")
            expectThat(getResponse.status).isEqualTo(HttpStatusCode.OK)

            val coverageExists = objectMapper.readValue(getResponse.bodyAsText(), CoverageExists::class.java)
            assertNotNull(coverageExists)

            expectThat(coverageExists.exists).isTrue()
        }

    @Test
    fun `when report does not have coverage data should return false`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            testRunDBGenerator.createSimpleTestRun(publicId)

            val response = client.get("/run/$publicId/coverage/exists")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val coverageExists = objectMapper.readValue(response.bodyAsText(), CoverageExists::class.java)
            assertNotNull(coverageExists)

            expectThat(coverageExists.exists).isFalse()
        }
}
