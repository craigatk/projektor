package projektor.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import projektor.pullrequest.PullRequestFailureContextService

internal const val GET_PULL_REQUEST_FAILING_TEST_CONTEXT = "get_pull_request_failing_test_context"

internal fun Server.addPullRequestFailingTestContextTool(pullRequestFailureContextService: PullRequestFailureContextService) =
    addReadOnlyTool(
        name = GET_PULL_REQUEST_FAILING_TEST_CONTEXT,
        description =
            "Fetches the diagnostic context (failure message, stack trace, stdout/stderr) for every " +
                "currently-failing test in the most recent Projektor test run recorded for a GitHub pull " +
                "request, so it can be used to diagnose why those tests are failing. Identify the pull " +
                "request either with pullRequestUrl, or with orgName, repoName, and pullRequestNumber " +
                "together. Each failing test's test_run_public_id, test_suite_idx, and test_case_idx can be " +
                "passed to $GET_TEST_CASE_HISTORY to see whether it has been failing before.",
        properties = { pullRequestProperties() },
    ) { arguments ->
        val location = arguments.requiredPullRequestLocation()

        pullRequestFailureContextService.fetchFailureContext(location.orgName, location.repoName, location.pullRequestNumber)
            ?.let { jsonResult(it) }
            ?: textResult("No test run found for $location")
    }
