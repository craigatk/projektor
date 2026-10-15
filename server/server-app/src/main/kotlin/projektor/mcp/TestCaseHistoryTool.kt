package projektor.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import projektor.server.api.PublicId
import projektor.testcase.TestCaseService

internal const val GET_TEST_CASE_HISTORY = "get_test_case_history"

private const val DEFAULT_MAX_RUNS = 30
private const val MAX_MAX_RUNS = 100

internal fun Server.addTestCaseHistoryTool(testCaseService: TestCaseService) =
    addReadOnlyTool(
        name = GET_TEST_CASE_HISTORY,
        description =
            "Fetches a test case's pass/fail history across the repository's recent Projektor test runs, most " +
                "recent first, starting from the given test run. Also reports first_failure (where the current " +
                "streak of failures began) and last_passed_before_failure, including their branch, commit SHA, " +
                "and pull request number, which helps tell a new regression from a long-standing or flaky " +
                "failure and narrow down which change broke it. The test case is identified by the " +
                "test_run_public_id, test_suite_idx, and test_case_idx returned by " +
                "$GET_PULL_REQUEST_FAILING_TEST_CONTEXT or $GET_FLAKY_TESTS.",
        required = listOf("testRunPublicId", "testSuiteIdx", "testCaseIdx"),
        properties = {
            stringProperty("testRunPublicId", "Public ID of a Projektor test run containing the test case")
            integerProperty("testSuiteIdx", "Index of the test suite within that test run")
            integerProperty("testCaseIdx", "Index of the test case within that test suite")
            integerProperty("maxRuns", "Maximum number of test runs to include (default $DEFAULT_MAX_RUNS, max $MAX_MAX_RUNS)")
        },
    ) { arguments ->
        val testRunPublicId = arguments.requiredString("testRunPublicId")
        val testSuiteIdx = arguments.requiredInt("testSuiteIdx")
        val testCaseIdx = arguments.requiredInt("testCaseIdx")
        val maxRuns = (arguments.int("maxRuns") ?: DEFAULT_MAX_RUNS).coerceIn(1, MAX_MAX_RUNS)

        val history = testCaseService.fetchTestCaseHistory(PublicId(testRunPublicId), testSuiteIdx, testCaseIdx, maxRuns)

        if (history.entries.isEmpty()) {
            textResult(
                "No history found for test case $testCaseIdx in test suite $testSuiteIdx of test run $testRunPublicId " +
                    "(either it doesn't exist, or the test run has no git metadata to find other runs of the same repository)",
            )
        } else {
            jsonResult(history)
        }
    }
