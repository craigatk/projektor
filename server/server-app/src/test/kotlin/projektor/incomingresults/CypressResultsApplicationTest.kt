package projektor.incomingresults

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
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
    fun shouldSaveCypressResultsAndReadThemBackFromDatabase() {
        val requestBody = resultsXmlLoader.cypressResults().joinToString("\n")

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/results") {
                addHeader(HttpHeaders.ContentType, "text/plain")
                setBody(requestBody)
            }.apply {
                val (publicId, testRun) = waitForTestRunSaveToComplete(response)

                val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)

                testSuites.forEach { testSuite ->
                    val testCases = testCaseDao.fetchByTestSuiteId(testSuite.id)
                    expectThat(testCases).isNotEmpty()
                }

                val testSuiteService: TestSuiteService = application.get()

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
        }
    }

    @Test
    fun `when one Cypress test suite is empty should save other one`() {
        val requestBody = GroupedResultsXmlLoader().wrapResultsXmlsInGroup(
            listOf(
                resultsXmlLoader.cypressEmptyTestSuites(),
                resultsXmlLoader.cypressAttachmentsSpecWithFilePath()
            )
        )

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }.apply {
                val (publicId, _) = waitForTestRunSaveToComplete(response)

                val testRunService: TestRunService = application.get()

                val testRun = runBlocking { testRunService.fetchTestRun(publicId) }
                assertNotNull(testRun)
                expectThat(testRun) {
                    get { summary }.get { totalTestCount }.isEqualTo(2)
                }
            }
        }
    }
}
