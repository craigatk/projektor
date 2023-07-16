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
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

class SaveTestCaseSystemOutErrApplicationTest : ApplicationTestCase() {
    @Test
    fun `should save system out and err at the test case level`() {
        val resultsBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(ResultsXmlLoader().gradleSingleTestCaseSystemOutFail())

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(resultsBody)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val (publicId, testRunDB) = waitForTestRunSaveToComplete(response)

                val testRunService: TestRunService = application.get()
                val testSuiteService: TestSuiteService = application.get()
                val testCaseService: TestCaseService = application.get()

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
    }
}
