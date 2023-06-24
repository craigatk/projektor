package projektor.metadata

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.metadata.TestRunMetadata
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

class ResultsMetadataApplicationTest : ApplicationTestCase() {
    @Test
    fun `should get results metadata`() {
        val publicId = randomPublicId()
        val anotherPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/metadata") {
                val testRun = testRunDBGenerator.createSimpleTestRun(publicId)
                testRunDBGenerator.addResultsMetadata(testRun, true)

                val anotherTestRun = testRunDBGenerator.createSimpleTestRun(anotherPublicId)
                testRunDBGenerator.addResultsMetadata(anotherTestRun, false)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val metadata = objectMapper.readValue(response.content, TestRunMetadata::class.java)

                expectThat(metadata).isNotNull().and {
                    get { ci }.isTrue()
                }
            }
        }
    }
}
