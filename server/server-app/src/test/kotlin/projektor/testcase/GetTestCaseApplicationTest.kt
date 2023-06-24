package projektor.testcase

import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.createTestCase
import projektor.createTestRun
import projektor.createTestSuite
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestCase
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

class GetTestCaseApplicationTest : ApplicationTestCase() {
    @Test
    fun `should fetch test case from database`() {
        val publicId = randomPublicId()
        val testSuiteIdx = 1
        val testCaseIdx = 2

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/suite/$testSuiteIdx/case/$testCaseIdx") {
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(
                        TestSuiteData(
                            "testSuite1",
                            listOf("testSuite1TestCase1"),
                            listOf("testSuite1TestCase2"),
                            listOf()
                        ),
                        TestSuiteData(
                            "testSuite2",
                            listOf("testSuite2TestCase1", "testSuite2TestCase2", "testSuite2TestCase3"),
                            listOf(),
                            listOf()
                        )
                    )
                )
            }.apply {
                val responseTestCase = objectMapper.readValue(response.content, TestCase::class.java)

                expectThat(responseTestCase)
                    .isNotNull()
                    .and {
                        get { name }.isEqualTo("testSuite1TestCase2")
                        get { className }.isEqualTo("testSuite1TestCase2ClassName")
                        get { idx }.isEqualTo(2)
                        get { testSuiteIdx }.isEqualTo(1)
                        get { hasSystemOut }.isFalse()
                        get { hasSystemErr }.isFalse()
                    }

                val failure = responseTestCase.failure

                expectThat(failure)
                    .isNotNull()
                    .and {
                        get { failureMessage }.isEqualTo("testSuite1TestCase2 failure message")
                        get { failureText }.isEqualTo("testSuite1TestCase2 failure text")
                        get { failureType }.isEqualTo("testSuite1TestCase2 failure type")
                    }
            }
        }
    }

    @Test
    fun `when test suite has system out and err should include it in test case properties`() {
        val publicId = randomPublicId()
        val testSuiteIdx = 1
        val testCaseIdx = 2

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/suite/$testSuiteIdx/case/$testCaseIdx") {
                val testRun = createTestRun(publicId, 1)
                testRunDao.insert(testRun)

                val testSuite1 = createTestSuite(testRun.id, "ShouldGet1", testSuiteIdx)
                testSuite1.packageName = "com.example"
                testSuite1.hasSystemOut = true
                testSuite1.hasSystemErr = true
                testSuiteDao.insert(testSuite1)

                val testSuite1Case = createTestCase(testSuite1.id, "testSuite1Case", testCaseIdx, true)
                testCaseDao.insert(testSuite1Case)
            }.apply {
                val responseTestCase = objectMapper.readValue(response.content, TestCase::class.java)

                expectThat(responseTestCase)
                    .isNotNull()
                    .and {
                        get { hasSystemOut }.isTrue()
                        get { hasSystemErr }.isTrue()
                    }
            }
        }
    }
}
