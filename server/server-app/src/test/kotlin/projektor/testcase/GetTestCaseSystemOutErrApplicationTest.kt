package projektor.testcase

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.server.api.TestOutput
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import kotlin.test.assertNotNull

class GetTestCaseSystemOutErrApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should get system out and err at the test case level`() =
        testSuspend {
            val resultsBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(ResultsXmlLoader().gradleSingleTestCaseSystemOutFail())

            val postResponse = postGroupedResultsJSON(resultsBody)

            expectThat(postResponse.status).isEqualTo(HttpStatusCode.OK)

            val (publicId, _) = waitForTestRunSaveToComplete(postResponse)

            val testSuiteIdx = 1
            val testCaseIdx = 1

            val getSystemOutResponse = testClient.get("/run/${publicId.id}/suite/$testSuiteIdx/case/$testCaseIdx/systemOut")
            expectThat(getSystemOutResponse.status).isEqualTo(HttpStatusCode.OK)
            val testOutput = objectMapper.readValue(getSystemOutResponse.bodyAsText(), TestOutput::class.java)
            assertNotNull(testOutput)
            expectThat(testOutput.value).isNotNull().contains("HikariPool-1 - Exception during pool initialization")

            val getSystemErrResponse = testClient.get("/run/${publicId.id}/suite/$testSuiteIdx/case/$testCaseIdx/systemErr")
            expectThat(getSystemErrResponse.status).isEqualTo(HttpStatusCode.OK)
            val testErr = objectMapper.readValue(getSystemErrResponse.bodyAsText(), TestOutput::class.java)
            assertNotNull(testErr)
            expectThat(testErr.value).isNotNull().contains("System error")
        }
}
