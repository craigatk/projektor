package projektor.testcase

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.server.api.PublicId
import projektor.server.api.TestOutput
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import kotlin.test.assertNotNull

class GetTestCaseSystemOutErrApplicationTest : ApplicationTestCase() {
    @Test
    fun `should get system out and err at the test case level`() {
        val resultsBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(ResultsXmlLoader().gradleSingleTestCaseSystemOutFail())

        var insertedPublicId: PublicId

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(resultsBody)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val (publicId, _) = waitForTestRunSaveToComplete(response)
                insertedPublicId = publicId
            }

            val testSuiteIdx = 1
            val testCaseIdx = 1

            handleRequest(HttpMethod.Get, "/run/${insertedPublicId.id}/suite/$testSuiteIdx/case/$testCaseIdx/systemOut").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
                val responseOutput = objectMapper.readValue(response.content, TestOutput::class.java)
                assertNotNull(responseOutput)

                expectThat(responseOutput.value).isNotNull().contains("HikariPool-1 - Exception during pool initialization")
            }

            handleRequest(HttpMethod.Get, "/run/${insertedPublicId.id}/suite/$testSuiteIdx/case/$testCaseIdx/systemErr").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)
                val responseOutput = objectMapper.readValue(response.content, TestOutput::class.java)
                assertNotNull(responseOutput)

                expectThat(responseOutput.value).isNotNull().contains("System error")
            }
        }
    }
}
