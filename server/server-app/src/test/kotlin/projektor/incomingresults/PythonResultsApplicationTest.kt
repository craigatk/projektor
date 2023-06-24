package projektor.incomingresults

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class PythonResultsApplicationTest : ApplicationTestCase() {
    @Test
    fun `should save passing pytest results`() {
        val requestBody = resultsXmlLoader.pytestPassing()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/results") {
                addHeader(HttpHeaders.ContentType, "text/plain")
                setBody(requestBody)
            }.apply {
                val (_, testRun) = waitForTestRunSaveToComplete(response)

                expectThat(testRun.passed).isTrue()

                val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)
                expectThat(testSuites).hasSize(1)

                val testSuite = testSuites[0]

                val testCases = testCaseDao.fetchByTestSuiteId(testSuite.id)
                expectThat(testCases).hasSize(1)

                val testCase = testCases[0]
                expectThat(testCase) {
                    get { className }.isEqualTo("test_sample1")
                    get { passed }.isTrue()
                }
            }
        }
    }

    @Test
    fun `should save failing pytest results`() {
        val requestBody = resultsXmlLoader.pytestFailing()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/results") {
                addHeader(HttpHeaders.ContentType, "text/plain")
                setBody(requestBody)
            }.apply {
                val (_, testRun) = waitForTestRunSaveToComplete(response)

                expectThat(testRun.passed).isFalse()

                val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)
                expectThat(testSuites).hasSize(1)
                val testSuite = testSuites[0]

                val testCases = testCaseDao.fetchByTestSuiteId(testSuite.id)
                expectThat(testCases).hasSize(1)

                val testCase = testCases[0]
                expectThat(testCase) {
                    get { className }.isEqualTo("test_sample1")
                    get { passed }.isFalse()
                }
            }
        }
    }
}
