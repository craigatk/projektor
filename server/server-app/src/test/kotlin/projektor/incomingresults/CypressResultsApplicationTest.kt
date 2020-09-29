package projektor.incomingresults

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.ktor.ext.get
import projektor.ApplicationTestCase
import projektor.testsuite.TestSuiteService
import strikt.api.expectThat
import strikt.assertions.isNotEmpty
import strikt.assertions.isTrue
import kotlin.test.assertNotNull

@KtorExperimentalAPI
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
}
