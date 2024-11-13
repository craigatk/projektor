package projektor.testcase

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
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
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should fetch failed test cases from database`() =
        testSuspend {
            val publicId = randomPublicId()

            testRunDBGenerator.createTestRun(
                publicId,
                listOf(
                    TestSuiteData(
                        "testSuite1",
                        listOf("testSuite1PassedTestCase1", "testSuite1PassedTestCase2"),
                        listOf("testSuite1FailedTestCase1", "testSuite1FailedTestCase2"),
                        listOf(),
                    ),
                    TestSuiteData(
                        "testSuite2",
                        listOf("testSuite2PassedTestCase1", "testSuite2PassedTestCase2"),
                        listOf("testSuite2FailedTestCase1"),
                        listOf(),
                    ),
                ),
            )

            val response = testClient.get("/run/${publicId.id}/cases/failed")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val responseContent = response.bodyAsText()
            assertNotNull(responseContent)
            val failedTestCases: List<TestCase> = objectMapper.readValue(responseContent)

            expectThat(failedTestCases)
                .hasSize(3)
                .map(TestCase::name)
                .contains(
                    "testSuite1FailedTestCase1",
                    "testSuite1FailedTestCase2",
                    "testSuite2FailedTestCase1",
                )

            expectThat(failedTestCases)
                .map(TestCase::failure)
                .all { isNotNull() }
        }
}
