package projektor.metadata

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.metadata.TestRunMetadata
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

class ResultsMetadataApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should get results metadata`() =
        testSuspend {
            val publicId = randomPublicId()
            val anotherPublicId = randomPublicId()

            val testRun = testRunDBGenerator.createSimpleTestRun(publicId)
            testRunDBGenerator.addResultsMetadata(testRun, true)

            val anotherTestRun = testRunDBGenerator.createSimpleTestRun(anotherPublicId)
            testRunDBGenerator.addResultsMetadata(anotherTestRun, false)

            val response = testClient.get("/run/$publicId/metadata")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val metadata = objectMapper.readValue(response.bodyAsText(), TestRunMetadata::class.java)

            expectThat(metadata).isNotNull().and {
                get { ci }.isTrue()
            }
        }
}
