package projektor.testcase

import com.fasterxml.jackson.core.type.TypeReference
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.*
import projektor.ApplicationTestCase
import projektor.TestRunDBGenerator
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestCase
import strikt.api.expectThat
import strikt.assertions.*

@KtorExperimentalAPI
class GetFailedTestCasesApplicationTest : ApplicationTestCase() {
    @Test
    fun `should fetch failed test cases from database`() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/${publicId.id}/cases/failed") {
                val testRunDBGenerator = TestRunDBGenerator(testRunDao, testSuiteDao, testCaseDao, testFailureDao)
                testRunDBGenerator.createTestRun(
                        publicId,
                        listOf(TestSuiteData(
                                "testSuite1",
                                listOf("testSuite1PassedTestCase1", "testSuite1PassedTestCase2"),
                                listOf("testSuite1FailedTestCase1", "testSuite1FailedTestCase2"),
                                listOf()
                        ),
                                TestSuiteData(
                                        "testSuite2",
                                        listOf("testSuite2PassedTestCase1", "testSuite2PassedTestCase2"),
                                        listOf("testSuite2FailedTestCase1"),
                                        listOf()
                                )
                        )
                )
            }.apply {
                val failedTestCases: List<TestCase> = objectMapper.readValue(response.content, object : TypeReference<List<TestCase>>() {})

                expectThat(failedTestCases)
                        .hasSize(3)
                        .map(TestCase::name)
                        .contains(
                                "testSuite1FailedTestCase1",
                                "testSuite1FailedTestCase2",
                                "testSuite2FailedTestCase1"
                        )

                expectThat(failedTestCases)
                        .map(TestCase::failure)
                        .all { isNotNull() }
            }
        }
    }
}
