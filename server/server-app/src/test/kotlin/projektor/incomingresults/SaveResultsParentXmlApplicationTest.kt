package projektor.incomingresults

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

@KtorExperimentalAPI
class SaveResultsParentXmlApplicationTest : ApplicationTestCase() {
    @Test
    fun shouldParseRequestAndSaveResultsWithMultipleResultsWrappedInParentXmlTest() {
        val requestBody = listOf(resultsXmlLoader.passing(), resultsXmlLoader.failing()).joinToString("\n")

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/results") {
                addHeader(HttpHeaders.ContentType, "text/plain")
                setBody(requestBody)
            }.apply {
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
    }
}
