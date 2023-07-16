package projektor.incomingresults

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.ktor.ext.get
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.testcase.TestCaseService
import projektor.testrun.TestRunService
import projektor.testsuite.TestSuiteService
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class K6ResultsApplicationTest : ApplicationTestCase() {
    @Test
    fun `should save results from k6 run to database`() {
        val resultsBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(ResultsXmlLoader().k6Example())

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(resultsBody)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val (publicId, _) = waitForTestRunSaveToComplete(response)

                val testRunService: TestRunService = application.get()
                val testSuiteService: TestSuiteService = application.get()
                val testCaseService: TestCaseService = application.get()

                val testRun = runBlocking { testRunService.fetchTestRun(publicId) }
                expectThat(testRun).isNotNull().and {
                    get { summary }.get { totalTestCount }.isEqualTo(2)
                }

                val testSuite = runBlocking { testSuiteService.fetchTestSuite(publicId, 1) }
                expectThat(testSuite).isNotNull().and {
                    get { passingCount }.isEqualTo(1)
                    get { failureCount }.isEqualTo(1)
                    get { packageName }.isNotNull().isEqualTo("k6/test/example.k6.js")
                    get { className }.isEqualTo("example")
                }

                val testCase1 = runBlocking { testCaseService.fetchTestCase(publicId, 1, 1) }
                expectThat(testCase1).isNotNull()

                val testCase2 = runBlocking { testCaseService.fetchTestCase(publicId, 1, 2) }
                expectThat(testCase2).isNotNull()
            }
        }
    }
}
