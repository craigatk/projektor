package projektor.mcp

import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.util.randomOrgAndRepo
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class McpTestCaseHistoryApplicationTest : ApplicationTestCase() {
    @Test
    fun `should return history of test case across repository test runs`() =
        projektorTestApplication {
            val repoName = randomOrgAndRepo()

            val passingPublicId = randomPublicId()
            testRunDBGenerator.createTestRunInRepo(
                passingPublicId,
                listOf(TestSuiteData("projektor.suite", listOf("sometimesFails"), listOf(), listOf())),
                repoName,
                true,
                null,
            )

            val failingPublicId = randomPublicId()
            testRunDBGenerator.createTestRunInRepo(
                failingPublicId,
                listOf(TestSuiteData("projektor.suite", listOf(), listOf("sometimesFails"), listOf())),
                repoName,
                true,
                null,
            )

            val response =
                client.callMcpTool(
                    GET_TEST_CASE_HISTORY,
                    mapOf("testRunPublicId" to failingPublicId.id, "testSuiteIdx" to 1, "testCaseIdx" to 1),
                )

            expectThat(response.isError).isFalse()

            val history = response.json
            expectThat(history.path("entries").size()).isEqualTo(2)
            expectThat(history.path("entries").path(0).path("public_id").asText()).isEqualTo(failingPublicId.id)
            expectThat(history.path("entries").path(1).path("public_id").asText()).isEqualTo(passingPublicId.id)
            expectThat(history.path("first_failure").path("public_id").asText()).isEqualTo(failingPublicId.id)
            expectThat(history.path("last_passed_before_failure").path("public_id").asText()).isEqualTo(passingPublicId.id)
        }

    @Test
    fun `should report when test case is not found`() =
        projektorTestApplication {
            val response =
                client.callMcpTool(
                    GET_TEST_CASE_HISTORY,
                    mapOf("testRunPublicId" to randomPublicId().id, "testSuiteIdx" to 1, "testCaseIdx" to 1),
                )

            expectThat(response.isError).isFalse()
            expectThat(response.text).contains("No history found")
        }

    @Test
    fun `should return error when test case indexes are missing or not integers`() =
        projektorTestApplication {
            val missingResponse = client.callMcpTool(GET_TEST_CASE_HISTORY, mapOf("testRunPublicId" to "abc"))

            expectThat(missingResponse.isError).isTrue()
            expectThat(missingResponse.text).isEqualTo("testSuiteIdx is required")

            val invalidResponse =
                client.callMcpTool(
                    GET_TEST_CASE_HISTORY,
                    mapOf("testRunPublicId" to "abc", "testSuiteIdx" to "one", "testCaseIdx" to 1),
                )

            expectThat(invalidResponse.isError).isTrue()
            expectThat(invalidResponse.text).isEqualTo("testSuiteIdx must be an integer")
        }
}
