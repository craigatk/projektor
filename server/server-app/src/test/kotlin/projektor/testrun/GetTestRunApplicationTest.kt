package projektor.testrun

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestRunDBGenerator
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestRun
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

@KtorExperimentalAPI
class GetTestRunApplicationTest : ApplicationTestCase() {
    @Test
    fun shouldFetchTestRunFromDatabase() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId") {
                val testRunDBGenerator = TestRunDBGenerator(testRunDao, testSuiteDao, testCaseDao, testFailureDao)

                testRunDBGenerator.createTestRun(
                        publicId,
                        listOf(
                                TestSuiteData("testSuite1",
                                        listOf("testSuite1TestCase1", "testSuite1TestCase2"),
                                        listOf(),
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
                val responseRun = objectMapper.readValue(response.content, TestRun::class.java)
                assertNotNull(responseRun)

                expectThat(responseRun.id).isEqualTo(publicId.id)

                val testSuites = responseRun.testSuites
                assertNotNull(testSuites)
                expectThat(testSuites).hasSize(2)

                val testSuite1 = testSuites.find { it.className == "testSuite1" }
                assertNotNull(testSuite1)

                val testSuite1TestCases = testSuite1.testCases
                assertNotNull(testSuite1TestCases)
                expectThat(testSuite1TestCases).hasSize(2)

                val testSuite2 = testSuites.find { it.className == "testSuite2" }
                assertNotNull(testSuite2)

                val testSuite2TestCases = testSuite2.testCases
                assertNotNull(testSuite2TestCases)
                expectThat(testSuite2TestCases).hasSize(3)
            }
        }
    }
}
