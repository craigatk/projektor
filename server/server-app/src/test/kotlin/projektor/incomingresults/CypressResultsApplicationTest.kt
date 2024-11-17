package projektor.incomingresults

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.ktor.ext.get
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.testrun.TestRunService
import projektor.testsuite.TestSuiteService
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import strikt.assertions.isTrue
import kotlin.test.assertNotNull

class CypressResultsApplicationTest : ApplicationTestCase() {
    @Test
    fun shouldSaveCypressResultsAndReadThemBackFromDatabase() =
        projektorTestApplication {
            val requestBody = resultsXmlLoader.cypressResults().joinToString("\n")

            val response = client.postResultsPlainText(requestBody)

            val (publicId, testRun) = waitForTestRunSaveToComplete(response)

            val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)

            testSuites.forEach { testSuite ->
                val testCases = testCaseDao.fetchByTestSuiteId(testSuite.id)
                expectThat(testCases).isNotEmpty()
            }

            val testSuiteService: TestSuiteService = getApplication().get()

            testSuites.forEach { testSuiteDB ->
                val testSuite = runBlocking { testSuiteService.fetchTestSuite(publicId, testSuiteDB.idx) }
                assertNotNull(testSuite)
                val testCases = testSuite.testCases
                assertNotNull(testCases)
                expectThat(testCases).isNotEmpty()
            }

            testSuites.forEach { testSuiteDB ->
                val testSuiteSystemOut = runBlocking { testSuiteService.fetchTestSuiteSystemOut(publicId, testSuiteDB.idx) }
                expectThat(testSuiteSystemOut.value.isNullOrEmpty()).isTrue()
            }
        }

    @Test
    fun `when one Cypress test suite is empty should save other one`() =
        projektorTestApplication {
            val requestBody =
                GroupedResultsXmlLoader().wrapResultsXmlsInGroup(
                    listOf(
                        resultsXmlLoader.cypressEmptyTestSuites(),
                        resultsXmlLoader.cypressAttachmentsSpecWithFilePath(),
                    ),
                )

            val response = client.postGroupedResultsJSON(requestBody)

            val (publicId, _) = waitForTestRunSaveToComplete(response)

            val testRunService: TestRunService = getApplication().get()

            val testRun = runBlocking { testRunService.fetchTestRun(publicId) }
            assertNotNull(testRun)
            expectThat(testRun) {
                get { summary }.get { totalTestCount }.isEqualTo(2)
            }
        }
}
