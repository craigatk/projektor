package projektor.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import projektor.pullrequest.PullRequestCoverageService

internal const val GET_COVERAGE_DELTA = "get_coverage_delta"

private const val DEFAULT_MAX_CHANGED_FILES = 50
private const val MAX_MAX_CHANGED_FILES = 500

internal fun Server.addCoverageDeltaTool(pullRequestCoverageService: PullRequestCoverageService) =
    addReadOnlyTool(
        name = GET_COVERAGE_DELTA,
        description =
            "Compares the code coverage of a GitHub pull request's most recent Projektor test run that has " +
                "coverage against the most recent main branch run before it. Returns the overall and per-module " +
                "(group) line/statement/branch coverage with covered_percentage_delta, plus the files whose line " +
                "coverage changed or that are new since the baseline - biggest coverage drops first - with their " +
                "missed and partially-covered line numbers. Use it to find where a pull request lost coverage and " +
                "which lines need tests. Identify the pull request either with pullRequestUrl, or with orgName, " +
                "repoName, and pullRequestNumber together.",
        properties = {
            pullRequestProperties()
            integerProperty(
                "maxChangedFiles",
                "Maximum number of changed files to include (default $DEFAULT_MAX_CHANGED_FILES, max $MAX_MAX_CHANGED_FILES)",
            )
        },
    ) { arguments ->
        val location = arguments.requiredPullRequestLocation()
        val maxChangedFiles = (arguments.int("maxChangedFiles") ?: DEFAULT_MAX_CHANGED_FILES).coerceIn(1, MAX_MAX_CHANGED_FILES)

        pullRequestCoverageService.fetchCoverageDelta(location.orgName, location.repoName, location.pullRequestNumber, maxChangedFiles)
            ?.let { jsonResult(it) }
            ?: textResult("No test run with coverage found for $location")
    }
