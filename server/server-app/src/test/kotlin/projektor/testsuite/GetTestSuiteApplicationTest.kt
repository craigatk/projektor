package projektor.testsuite

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.*
import projektor.ApplicationTestCase
import projektor.TestRunDBGenerator
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestSuite
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

@KtorExperimentalAPI
class GetTestSuiteApplicationTest : ApplicationTestCase() {
    @Test
    fun shouldFetchTestSuiteFromDatabase() {
        val publicId = randomPublicId()
        val testSuiteIdx = 1

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/suite/$testSuiteIdx") {
                val testRunDBGenerator = TestRunDBGenerator(testRunDao, testSuiteDao, testCaseDao, testFailureDao)
                testRunDBGenerator.createTestRun(
                        publicId,
                        listOf(
                                TestSuiteData("testSuite1",
                                        listOf("testSuite1TestCase1"),
                                        listOf("testSuite1TestCase2"),
                                        listOf()
                                ),
                                TestSuiteData("testSuite2",
                                        listOf("testSuite2TestCase1", "testSuite2TestCase2", "testSuite2TestCase3"),
                                        listOf(),
                                        listOf()
                                )
                        )
                )
            }.apply {
                val responseContent = response.content
                assertNotNull(responseContent)

                val responseSuite = objectMapper.readValue(responseContent, TestSuite::class.java)
                assertNotNull(responseSuite)

                val testCases = responseSuite.testCases
                assertNotNull(testCases)

                expectThat(testCases).hasSize(2)

                val testCase1 = testCases.find { it.name == "testSuite1TestCase1" }
                assertNotNull(testCase1)
                expectThat(testCase1.testSuiteIdx).isEqualTo(testSuiteIdx)

                val testCase2 = testCases.find { it.name == "testSuite1TestCase2" }
                assertNotNull(testCase2)
                expectThat(testCase2.testSuiteIdx).isEqualTo(testSuiteIdx)

                val testCase1Failure = testCase2.failure
                assertNotNull(testCase1Failure)
            }
        }
    }
}
