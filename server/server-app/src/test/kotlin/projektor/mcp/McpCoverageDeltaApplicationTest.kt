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

class McpCoverageDeltaApplicationTest : ApplicationTestCase() {
    @Test
    fun `should compare pull request coverage with main branch baseline`() =
        projektorTestApplication {
            val repoName = randomOrgAndRepo()
            val pullRequestNumber = 42

            val baselinePublicId = randomPublicId()
            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = baselinePublicId,
                coverageText =
                    jacocoReport(
                        "server-app",
                        "com/example",
                        mapOf(
                            "Unchanged.kt" to Pair(listOf(1), listOf(2, 3, 4)),
                            "Dropped.kt" to Pair(listOf(), listOf(1, 2, 3, 4)),
                            "Improved.kt" to Pair(listOf(1, 2), listOf(3, 4)),
                        ),
                    ),
                repoName = repoName,
            )

            val pullRequestPublicId = randomPublicId()
            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = pullRequestPublicId,
                coverageText =
                    jacocoReport(
                        "server-app",
                        "com/example",
                        mapOf(
                            "Unchanged.kt" to Pair(listOf(1), listOf(2, 3, 4)),
                            "Dropped.kt" to Pair(listOf(3, 4), listOf(1, 2)),
                            "Improved.kt" to Pair(listOf(), listOf(1, 2, 3, 4)),
                            "Added.kt" to Pair(listOf(5), listOf(6)),
                        ),
                    ),
                repoName = repoName,
                branchName = "feature",
                pullRequestNumber = pullRequestNumber,
            )

            val response =
                client.callMcpTool(GET_COVERAGE_DELTA, mapOf("pullRequestUrl" to "https://github.com/$repoName/pull/$pullRequestNumber"))

            expectThat(response.isError).isFalse()

            val delta = response.json
            expectThat(delta.path("test_run_public_id").asText()).isEqualTo(pullRequestPublicId.id)
            expectThat(delta.path("baseline_test_run_public_id").asText()).isEqualTo(baselinePublicId.id)
            expectThat(delta.path("groups").path(0).path("name").asText()).isEqualTo("server-app")
            expectThat(delta.path("overall_stats").path("line_stat").has("covered_percentage_delta")).isTrue()

            val changedFiles = delta.path("changed_files")
            expectThat(changedFiles.map { it.path("file_path").asText() })
                .isEqualTo(listOf("com/example/Dropped.kt", "com/example/Added.kt", "com/example/Improved.kt"))

            val droppedFile = changedFiles.path(0)
            expectThat(droppedFile.path("line_covered_percentage_delta").decimalValue().toInt()).isEqualTo(-50)
            expectThat(droppedFile.path("missed_lines").map { it.asInt() }).isEqualTo(listOf(3, 4))

            val addedFile = changedFiles.path(1)
            expectThat(addedFile.path("line_covered_percentage_delta").isNull).isTrue()
            expectThat(delta.path("changed_files_truncated").asBoolean()).isFalse()
        }

    @Test
    fun `should report when pull request has no coverage`() =
        projektorTestApplication {
            val response =
                client.callMcpTool(
                    GET_COVERAGE_DELTA,
                    mapOf("orgName" to "org", "repoName" to randomOrgAndRepo().substringAfter("/"), "pullRequestNumber" to 3),
                )

            expectThat(response.isError).isFalse()
            expectThat(response.text).contains("No test run with coverage found for pull request 3")
        }
}
