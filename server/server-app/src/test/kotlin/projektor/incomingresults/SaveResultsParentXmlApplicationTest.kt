package projektor.incomingresults

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.assertNotNull
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilNotNull
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.server.api.results.SaveResultsResponse
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

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
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val resultsResponse = objectMapper.readValue(response.content, SaveResultsResponse::class.java)

                val publicId = resultsResponse.id
                assertNotNull(publicId)

                val testRun = await untilNotNull { testRunDao.fetchOneByPublicId(publicId) }
                assertNotNull(testRun)

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
