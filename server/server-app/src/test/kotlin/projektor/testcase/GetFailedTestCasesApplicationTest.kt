package projektor.testcase

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.TestCase
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.map
import kotlin.test.assertNotNull

class GetFailedTestCasesApplicationTest : ApplicationTestCase() {
    @Test
    fun `should fetch failed test cases from database`() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/${publicId.id}/cases/failed") {
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(
                        TestSuiteData(
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
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val responseContent = response.content
                assertNotNull(responseContent)
                val failedTestCases: List<TestCase> = objectMapper.readValue(responseContent)

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
