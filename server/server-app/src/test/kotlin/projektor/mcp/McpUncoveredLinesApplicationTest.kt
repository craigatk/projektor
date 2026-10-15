package projektor.mcp

import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.util.randomOrgAndRepo
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class McpUncoveredLinesApplicationTest : ApplicationTestCase() {
    private val coverageReport =
        jacocoReport(
            "server-app",
            "com/example",
            mapOf(
                "Foo.kt" to Pair(listOf(7, 9), listOf(1, 2, 3)),
                "Bar.kt" to Pair(listOf(), listOf(1)),
            ),
        )

    @Test
    fun `should return uncovered lines for file in test run matched by longer repository path`() =
        projektorTestApplication {
            val publicId = randomPublicId()
            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(publicId, coverageReport, randomOrgAndRepo())

            val response =
                client.callMcpTool(
                    GET_UNCOVERED_LINES,
                    mapOf("testRunPublicId" to publicId.id, "filePath" to "server/src/main/kotlin/com/example/Foo.kt"),
                )

            expectThat(response.isError).isFalse()

            val uncoveredLines = response.json
            expectThat(uncoveredLines.path("test_run_public_id").asText()).isEqualTo(publicId.id)
            expectThat(uncoveredLines.path("files").size()).isEqualTo(1)

            val file = uncoveredLines.path("files").path(0)
            expectThat(file.path("file_path").asText()).isEqualTo("com/example/Foo.kt")
            expectThat(file.path("group_name").asText()).isEqualTo("server-app")
            expectThat(file.path("missed_lines").map { it.asInt() }).isEqualTo(listOf(7, 9))
            expectThat(file.path("line_stat").path("missed").asInt()).isEqualTo(2)
        }

    @Test
    fun `should use most recent main branch run when only repository given`() =
        projektorTestApplication {
            val repoName = randomOrgAndRepo()
            val (orgPart, repoPart) = repoName.split("/")

            val publicId = randomPublicId()
            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(publicId, coverageReport, repoName)

            val response =
                client.callMcpTool(
                    GET_UNCOVERED_LINES,
                    mapOf("orgName" to orgPart, "repoName" to repoPart, "filePath" to "Foo.kt"),
                )

            expectThat(response.isError).isFalse()
            expectThat(response.json.path("test_run_public_id").asText()).isEqualTo(publicId.id)
        }

    @Test
    fun `should use most recent pull request run with coverage`() =
        projektorTestApplication {
            val repoName = randomOrgAndRepo()

            val publicId = randomPublicId()
            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId,
                coverageReport,
                repoName,
                branchName = "feature",
                pullRequestNumber = 12,
            )

            val response =
                client.callMcpTool(
                    GET_UNCOVERED_LINES,
                    mapOf("pullRequestUrl" to "https://github.com/$repoName/pull/12", "filePath" to "com/example/Foo.kt"),
                )

            expectThat(response.isError).isFalse()
            expectThat(response.json.path("test_run_public_id").asText()).isEqualTo(publicId.id)
        }

    @Test
    fun `should report when file not in coverage`() =
        projektorTestApplication {
            val publicId = randomPublicId()
            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(publicId, coverageReport, randomOrgAndRepo())

            val response =
                client.callMcpTool(GET_UNCOVERED_LINES, mapOf("testRunPublicId" to publicId.id, "filePath" to "Baz.kt"))

            expectThat(response.isError).isFalse()
            expectThat(response.text).contains("No file matching Baz.kt found")
        }

    @Test
    fun `should report when repository has no coverage`() =
        projektorTestApplication {
            val response =
                client.callMcpTool(
                    GET_UNCOVERED_LINES,
                    mapOf("orgName" to "org", "repoName" to randomOrgAndRepo().substringAfter("/"), "filePath" to "Foo.kt"),
                )

            expectThat(response.isError).isFalse()
            expectThat(response.text).contains("No coverage found")
        }

    @Test
    fun `should return error when no test run source given`() =
        projektorTestApplication {
            val response = client.callMcpTool(GET_UNCOVERED_LINES, mapOf("filePath" to "Foo.kt"))

            expectThat(response.isError).isTrue()
            expectThat(response.text).contains("One of testRunPublicId")
        }
}
