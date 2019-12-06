package projektor.incomingresults

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilNotNull
import org.koin.ktor.ext.get
import projektor.ApplicationTestCase
import projektor.server.api.PublicId
import projektor.server.api.SaveResultsResponse
import projektor.testsuite.TestSuiteService
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import strikt.assertions.isTrue

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
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val resultsResponse = objectMapper.readValue(response.content, SaveResultsResponse::class.java)

                val publicId = resultsResponse.id
                assertNotNull(publicId)

                val testRun = await untilNotNull { testRunDao.fetchOneByPublicId(publicId) }
                assertNotNull(testRun)

                val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)

                testSuites.forEach { testSuite ->
                    val testCases = testCaseDao.fetchByTestSuiteId(testSuite.id)
                    expectThat(testCases).isNotEmpty()
                }

                val testSuiteService: TestSuiteService = application.get()

                testSuites.forEach { testSuiteDB ->
                    val testSuite = runBlocking { testSuiteService.fetchTestSuite(PublicId(publicId), testSuiteDB.idx) }
                    assertNotNull(testSuite)
                    val testCases = testSuite.testCases
                    assertNotNull(testCases)
                    expectThat(testCases).isNotEmpty()
                }

                testSuites.forEach { testSuiteDB ->
                    val testSuiteSystemOut = runBlocking { testSuiteService.fetchTestSuiteSystemOut(PublicId(publicId), testSuiteDB.idx) }
                    expectThat(testSuiteSystemOut.value.isNullOrEmpty()).isTrue()
                }
            }
        }
    }
}
