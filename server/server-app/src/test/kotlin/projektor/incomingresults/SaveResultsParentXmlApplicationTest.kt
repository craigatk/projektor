package projektor.incomingresults

import io.ktor.test.dispatcher.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

class SaveResultsParentXmlApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun shouldParseRequestAndSaveResultsWithMultipleResultsWrappedInParentXmlTest() =
        testSuspend {
            val requestBody = listOf(resultsXmlLoader.passing(), resultsXmlLoader.failing()).joinToString("\n")

            val response = postResultsPlainText(requestBody)

            val (_, testRun) = waitForTestRunSaveToComplete(response)

            val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)
            expectThat(testSuites).hasSize(2)

            val passingTestSuite = testSuites.find { it.className.contains("PassingSpec") }
            assertNotNull(passingTestSuite)
            expectThat(passingTestSuite.testCount).isEqualTo(1)
            expectThat(passingTestSuite.failureCount).isEqualTo(0)
            val passingSuiteTestCases = testCaseDao.fetchByTestSuiteId(passingTestSuite.id)
            expectThat(passingSuiteTestCases).hasSize(1)

            val failingTestSuite = testSuites.find { it.className.contains("FailingSpec") }
            assertNotNull(failingTestSuite)
            expectThat(failingTestSuite.testCount).isEqualTo(2)
            expectThat(failingTestSuite.failureCount).isEqualTo(2)
            val failingSuiteTestCases = testCaseDao.fetchByTestSuiteId(failingTestSuite.id)
            expectThat(failingSuiteTestCases).hasSize(2)
        }
}
