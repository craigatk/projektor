package projektor.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageService
import projektor.pullrequest.PullRequestCoverageService
import projektor.server.api.PublicId
import projektor.server.api.repository.BranchSearch
import projektor.server.api.repository.BranchType

internal const val GET_UNCOVERED_LINES = "get_uncovered_lines"

internal fun Server.addUncoveredLinesTool(
    coverageService: CoverageService,
    pullRequestCoverageService: PullRequestCoverageService,
    previousTestRunService: PreviousTestRunService,
) = addReadOnlyTool(
    name = GET_UNCOVERED_LINES,
    description =
        "Fetches the line numbers in a source file that tests don't cover (missed_lines) or only partially cover, " +
            "e.g. not every branch taken (partial_lines), along with the file's line and branch coverage, from " +
            "Projektor's coverage data. Use it to find which code still needs tests. filePath can be a " +
            "repository-relative path, a package-relative path, or just the file name; a file name matching more " +
            "than one file returns them all. The coverage comes from, in order of precedence: testRunPublicId if " +
            "given; else the pull request's most recent test run with coverage if pullRequestUrl or " +
            "pullRequestNumber is given; else the most recent main branch run with coverage in orgName/repoName.",
    required = listOf("filePath"),
    properties = {
        stringProperty("filePath", "Path of the source file, e.g. src/main/kotlin/com/example/Foo.kt, com/example/Foo.kt, or Foo.kt")
        stringProperty("testRunPublicId", "Public ID of a specific Projektor test run to read coverage from")
        pullRequestProperties()
        stringProperty(
            "projectName",
            "Optional project name, for repositories that publish results for multiple projects (main branch lookup only)",
        )
    },
) { arguments ->
    val filePath = arguments.requiredString("filePath")

    val testRunPublicId = arguments.string("testRunPublicId")
    val pullRequest = if (testRunPublicId == null) arguments.pullRequestLocationOrNull() else null
    val repository = if (testRunPublicId == null && pullRequest == null) arguments.repositoryLocationOrNull() else null

    val (publicId, runDescription) =
        when {
            testRunPublicId != null -> PublicId(testRunPublicId) to "test run $testRunPublicId"
            pullRequest != null ->
                pullRequestCoverageService.findMostRecentTestRunWithCoverage(
                    pullRequest.orgName,
                    pullRequest.repoName,
                    pullRequest.pullRequestNumber,
                ) to "the most recent test run with coverage for $pullRequest"
            repository != null ->
                previousTestRunService.findMostRecentRunWithCoverage(
                    repository.fullRepoName,
                    arguments.string("projectName"),
                    BranchSearch(branchType = BranchType.MAINLINE),
                )?.publicId to "the most recent main branch test run with coverage in ${repository.fullRepoName}"
            else -> throw McpToolArgumentException(
                "One of testRunPublicId, pullRequestUrl, orgName/repoName/pullRequestNumber, or orgName/repoName is required",
            )
        }

    when {
        publicId == null || !coverageService.coverageExists(publicId) -> textResult("No coverage found for $runDescription")
        else -> {
            val uncoveredLines = coverageService.findUncoveredLines(publicId, filePath)

            if (uncoveredLines.files.isEmpty()) {
                textResult("No file matching $filePath found in the coverage for $runDescription (${publicId.id})")
            } else {
                jsonResult(uncoveredLines)
            }
        }
    }
}
