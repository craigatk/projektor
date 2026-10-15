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

class McpFlakyTestsApplicationTest : ApplicationTestCase() {
    @Test
    fun `should return flaky tests in repository`() =
        projektorTestApplication {
            val repoName = randomOrgAndRepo()
            val (orgPart, repoPart) = repoName.split("/")

            val testSuiteDataList =
                listOf(
                    TestSuiteData(
                        "projektor.flakySuite",
                        listOf("passing1"),
                        listOf("flaky1"),
                        listOf(),
                    ),
                )

            val publicIds = (1..3).map { randomPublicId() }
            publicIds.forEach { publicId ->
                testRunDBGenerator.createTestRunInRepo(publicId, testSuiteDataList, repoName, true, null)
            }

            val response =
                client.callMcpTool(
                    GET_FLAKY_TESTS,
                    mapOf("orgName" to orgPart, "repoName" to repoPart, "failureThreshold" to 2),
                )

            expectThat(response.isError).isFalse()

            val flakyTests = response.json
            expectThat(flakyTests.path("repo").asText()).isEqualTo(repoName)
            expectThat(flakyTests.path("tests").size()).isEqualTo(1)

            val flakyTest = flakyTests.path("tests").path(0)
            expectThat(flakyTest.path("test_name").asText()).contains("flaky1")
            expectThat(flakyTest.path("failure_count").asInt()).isEqualTo(3)
            expectThat(flakyTest.path("latest_failure").path("test_run_public_id").asText()).isEqualTo(publicIds.last().id)
            expectThat(flakyTest.path("latest_failure").path("failure_message").asText()).contains("flaky1 failure message")
            expectThat(flakyTest.path("first_failure").path("test_run_public_id").asText()).isEqualTo(publicIds.first().id)
        }

    @Test
    fun `should report when repository has no flaky tests`() =
        projektorTestApplication {
            val response =
                client.callMcpTool(
                    GET_FLAKY_TESTS,
                    mapOf("orgName" to "unknown-org", "repoName" to randomOrgAndRepo().substringAfter("/")),
                )

            expectThat(response.isError).isFalse()
            expectThat(response.text).contains("No flaky tests found")
        }

    @Test
    fun `should return error when repository is missing`() =
        projektorTestApplication {
            val response = client.callMcpTool(GET_FLAKY_TESTS, mapOf("orgName" to "org"))

            expectThat(response.isError).isTrue()
            expectThat(response.text).isEqualTo("orgName and repoName are required")
        }

    @Test
    fun `should return error for invalid branch type`() =
        projektorTestApplication {
            val response =
                client.callMcpTool(GET_FLAKY_TESTS, mapOf("orgName" to "org", "repoName" to "repo", "branchType" to "feature"))

            expectThat(response.isError).isTrue()
            expectThat(response.text).contains("branchType must be one of")
        }
}
