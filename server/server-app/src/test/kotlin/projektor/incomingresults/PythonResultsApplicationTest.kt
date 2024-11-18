package projektor.incomingresults

import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class PythonResultsApplicationTest : ApplicationTestCase() {
    @Test
    fun `should save passing pytest results`() =
        projektorTestApplication {
            val requestBody = resultsXmlLoader.pytestPassing()

            val response = client.postResultsPlainText(requestBody)

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

    @Test
    fun `should save failing pytest results`() =
        projektorTestApplication {
            val requestBody = resultsXmlLoader.pytestFailing()

            val response = client.postResultsPlainText(requestBody)

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
