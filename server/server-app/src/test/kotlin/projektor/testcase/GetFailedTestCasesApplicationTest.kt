package projektor.testcase

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
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

    @Test
    fun `should include attachment when test case has one`() {
        val publicId = randomPublicId()

        attachmentsEnabled = true

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/${publicId.id}/cases/failed") {
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(
                        TestSuiteData(
                            "testSuite1",
                            listOf("testSuite1PassedTestCase1", "testSuite1PassedTestCase2"),
                            listOf("testSuite1 FailedTestCase 1", "testSuite1 FailedTestCase 2"),
                            listOf()
                        ),
                    )
                )

                testRunDBGenerator.addAttachment(publicId, "object-1", "testSuite1 FailedTestCase 1.png")
                testRunDBGenerator.addAttachment(publicId, "object-2", "testSuite1 FailedTestCase 2.png")
            }.apply {
                val failedTestCases: List<TestCase> = objectMapper.readValue(response.content!!)

                expectThat(failedTestCases)
                    .hasSize(2)

                val failedTestCase1 = failedTestCases.find { it.name == "testSuite1 FailedTestCase 1" }
                expectThat(failedTestCase1).isNotNull().and {
                    get { attachments }.isNotNull()
                        .hasSize(1)
                        .any { get { fileName }.isEqualTo("testSuite1 FailedTestCase 1.png") }
                }

                val failedTestCase2 = failedTestCases.find { it.name == "testSuite1 FailedTestCase 2" }
                expectThat(failedTestCase2).isNotNull().and {
                    get { attachments }
                        .isNotNull()
                        .hasSize(1)
                        .any { get { fileName }.isEqualTo("testSuite1 FailedTestCase 2.png") }
                }
            }
        }
    }
}
