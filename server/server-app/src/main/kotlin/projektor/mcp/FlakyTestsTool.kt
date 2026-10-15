package projektor.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import projektor.repository.testrun.RepositoryTestRunService
import projektor.server.api.TestCase
import projektor.server.api.repository.BranchType
import projektor.server.api.repository.RepositoryFlakyTest
import java.math.BigDecimal
import java.time.LocalDateTime

internal const val GET_FLAKY_TESTS = "get_flaky_tests"

private const val DEFAULT_MAX_RUNS = 50
private const val MAX_MAX_RUNS = 500
private const val DEFAULT_FAILURE_THRESHOLD = 5
private const val MAX_FAILURE_TEXT_LINES = 30

// A compact view of RepositoryFlakyTest - the full one carries three complete TestCases (with stack
// traces) per flaky test, which bloats the tool result for repos with many flaky tests.
private data class McpFlakyTests(
    val repo: String,
    val project: String?,
    val maxRuns: Int,
    val failureCountThreshold: Int,
    val branchType: BranchType,
    val tests: List<McpFlakyTest>,
)

private data class McpFlakyTest(
    val testName: String,
    val className: String?,
    val fileName: String?,
    val failureCount: Int,
    val failurePercentage: BigDecimal,
    val firstFailure: McpTestCaseFailure,
    val latestFailure: McpTestCaseFailure,
)

private data class McpTestCaseFailure(
    val testRunPublicId: String,
    val testSuiteIdx: Int,
    val testCaseIdx: Int,
    val createdTimestamp: LocalDateTime,
    val failureMessage: String?,
    val failureText: String?,
)

private fun TestCase.toMcpTestCaseFailure(includeFailureText: Boolean) =
    McpTestCaseFailure(
        testRunPublicId = publicId,
        testSuiteIdx = testSuiteIdx,
        testCaseIdx = idx,
        createdTimestamp = createdTimestamp,
        failureMessage = failure?.failureMessage,
        failureText =
            if (includeFailureText) {
                failure?.failureText?.lineSequence()?.take(MAX_FAILURE_TEXT_LINES)?.joinToString("\n")
            } else {
                null
            },
    )

private fun RepositoryFlakyTest.toMcpFlakyTest() =
    McpFlakyTest(
        testName = testCase.fullName,
        className = testCase.className,
        fileName = testCase.fileName,
        failureCount = failureCount,
        failurePercentage = failurePercentage,
        firstFailure = firstTestCase.toMcpTestCaseFailure(includeFailureText = false),
        latestFailure = latestTestCase.toMcpTestCaseFailure(includeFailureText = true),
    )

internal fun Server.addFlakyTestsTool(repositoryTestRunService: RepositoryTestRunService) =
    addReadOnlyTool(
        name = GET_FLAKY_TESTS,
        description =
            "Lists the flaky tests in a GitHub repository - tests that failed in at least failureThreshold of " +
                "the repository's most recent maxRuns Projektor test runs - most frequently failing first, with " +
                "the latest failure's message and (truncated) stack trace. Use it to find tests worth " +
                "stabilizing, or to check whether a failing test is a known flake before debugging it. Each " +
                "failure's test_run_public_id, test_suite_idx, and test_case_idx can be passed to " +
                "$GET_TEST_CASE_HISTORY for the test's full pass/fail timeline.",
        required = listOf("orgName", "repoName"),
        properties = {
            repositoryProperties()
            stringProperty("projectName", "Optional project name, for repositories that publish results for multiple projects")
            integerProperty("maxRuns", "How many of the repository's most recent test runs to look at (default $DEFAULT_MAX_RUNS)")
            integerProperty(
                "failureThreshold",
                "Minimum number of failures within those runs for a test to count as flaky (default $DEFAULT_FAILURE_THRESHOLD)",
            )
            enumProperty(
                "branchType",
                "ALL (default) to count failures on every branch including pull requests, MAINLINE for only the main branch",
                BranchType.entries.map { it.name },
            )
        },
    ) { arguments ->
        val repository = arguments.requiredRepositoryLocation()
        val projectName = arguments.string("projectName")
        val maxRuns = (arguments.int("maxRuns") ?: DEFAULT_MAX_RUNS).coerceIn(1, MAX_MAX_RUNS)
        val failureThreshold = (arguments.int("failureThreshold") ?: DEFAULT_FAILURE_THRESHOLD).coerceAtLeast(1)
        val branchType =
            arguments.string("branchType")?.let { value ->
                BranchType.entries.find { it.name.equals(value, ignoreCase = true) }
                    ?: throw McpToolArgumentException("branchType must be one of ${BranchType.entries.joinToString()}")
            } ?: BranchType.ALL

        val flakyTests =
            repositoryTestRunService.fetchFlakyTests(
                repoName = repository.fullRepoName,
                projectName = projectName,
                maxRuns = maxRuns,
                flakyFailureThreshold = failureThreshold,
                branchType = branchType,
            )

        if (flakyTests.isEmpty()) {
            textResult(
                "No flaky tests found in ${repository.fullRepoName}${projectName?.let { " project $it" }.orEmpty()} " +
                    "(no test failed at least $failureThreshold times in the most recent $maxRuns test runs)",
            )
        } else {
            jsonResult(
                McpFlakyTests(
                    repo = repository.fullRepoName,
                    project = projectName,
                    maxRuns = maxRuns,
                    failureCountThreshold = failureThreshold,
                    branchType = branchType,
                    tests = flakyTests.sortedByDescending { it.failureCount }.map { it.toMcpFlakyTest() },
                ),
            )
        }
    }
