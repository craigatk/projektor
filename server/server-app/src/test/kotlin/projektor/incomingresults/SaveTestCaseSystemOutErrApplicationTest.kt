package projektor.incomingresults

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
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

class SaveTestCaseSystemOutErrApplicationTest : ApplicationTestCase() {
    @Test
    fun `should save system out and err at the test case level`() =
        projektorTestApplication {
            val resultsBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(ResultsXmlLoader().gradleSingleTestCaseSystemOutFail())

            val response = client.postGroupedResultsJSON(resultsBody)
            val (publicId, testRunDB) = waitForTestRunSaveToComplete(response)

            val testRunService: TestRunService = getApplication().get()
            val testSuiteService: TestSuiteService = getApplication().get()
            val testCaseService: TestCaseService = getApplication().get()

            val testRun = runBlocking { testRunService.fetchTestRun(publicId) }
            expectThat(testRun).isNotNull().and {
                get { summary }.get { totalTestCount }.isEqualTo(1)
            }

            val testSuite = runBlocking { testSuiteService.fetchTestSuite(publicId, 1) }
            expectThat(testSuite).isNotNull().and {
                get { passingCount }.isEqualTo(0)
                get { failureCount }.isEqualTo(1)
                get { packageName }.isNotNull().isEqualTo("projektor.incomingresults")
                get { className }.isEqualTo("SaveGroupedResultsCompressedApplicationTest")
                get { hasSystemErr }.isFalse()
                get { hasSystemOut }.isFalse()
            }
            val testSuiteDBs = testSuiteDao.fetchByTestRunId(testRunDB.id)
            expectThat(testSuiteDBs).hasSize(1)
            val testSuiteDB = testSuiteDBs[0]

            val testCase = runBlocking { testCaseService.fetchTestCase(publicId, 1, 1) }
            expectThat(testCase).isNotNull().and {
                get { hasSystemOut }.isTrue()
                get { hasSystemErr }.isTrue()
                get { hasSystemOutTestCase }.isTrue()
                get { hasSystemErrTestCase }.isTrue()
                get { hasSystemErrTestSuite }.isFalse()
                get { hasSystemErrTestSuite }.isFalse()
            }

            val testCaseDBs = testCaseDao.fetchByTestSuiteId(testSuiteDB.id)
            expectThat(testCaseDBs).hasSize(1)

            expectThat(testCaseDBs[0].systemOut).contains("HikariPool-1 - Exception during pool initialization")
            expectThat(testCaseDBs[0].systemErr).contains("System error")
        }
}
