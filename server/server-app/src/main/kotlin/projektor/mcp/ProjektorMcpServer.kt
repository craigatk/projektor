package projektor.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageService
import projektor.pullrequest.PullRequestCoverageService
import projektor.pullrequest.PullRequestFailureContextService
import projektor.repository.testrun.RepositoryTestRunService
import projektor.testcase.TestCaseService

fun buildProjektorMcpServer(
    pullRequestFailureContextService: PullRequestFailureContextService,
    pullRequestCoverageService: PullRequestCoverageService,
    repositoryTestRunService: RepositoryTestRunService,
    testCaseService: TestCaseService,
    coverageService: CoverageService,
    previousTestRunService: PreviousTestRunService,
): Server {
    val server =
        Server(
            Implementation(name = "projektor", version = "1.0.0"),
            ServerOptions(
                capabilities =
                    ServerCapabilities(
                        tools = ServerCapabilities.Tools(listChanged = false),
                    ),
            ),
        )

    server.addPullRequestFailingTestContextTool(pullRequestFailureContextService)
    server.addFlakyTestsTool(repositoryTestRunService)
    server.addTestCaseHistoryTool(testCaseService)
    server.addCoverageDeltaTool(pullRequestCoverageService)
    server.addUncoveredLinesTool(coverageService, pullRequestCoverageService, previousTestRunService)

    return server
}
