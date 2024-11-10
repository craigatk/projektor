package projektor.incomingresults

import io.ktor.test.dispatcher.*
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
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should save results from k6 run to database`() =
        testSuspend {
            val resultsBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(ResultsXmlLoader().k6Example())

            val response = postGroupedResultsJSON(resultsBody)

            val (publicId, _) = waitForTestRunSaveToComplete(response)

            val testRunService: TestRunService = getApplication().get()
            val testSuiteService: TestSuiteService = getApplication().get()
            val testCaseService: TestCaseService = getApplication().get()

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
