package projektor.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import projektor.organization.coverage.OrganizationCoverageService
import projektor.repository.coverage.RepositoryCoverageService
import projektor.repository.testrun.RepositoryTestRunService
import projektor.server.api.repository.BranchSearch
import projektor.server.api.repository.BranchType
import projektor.server.api.repository.RepositoryFlakyTests
import projektor.server.api.repository.RepositoryTestRunSummaries
import kotlin.math.min

fun Route.api(
    organizationCoverageService: OrganizationCoverageService,
    repositoryCoverageService: RepositoryCoverageService,
    repositoryTestRunService: RepositoryTestRunService,
) {
    get("/api/v1/org/{orgName}/coverage/current") {
        val orgName = call.parameters.getOrFail("orgName")

        val organizationCoverage = organizationCoverageService.getCurrentCoverage(orgName)

        if (organizationCoverage.repositories.isNotEmpty()) {
            call.respond(HttpStatusCode.OK, organizationCoverage)
        } else {
            call.respond(HttpStatusCode.NoContent)
        }
    }

    get("/api/v1/repo/{orgPart}/{repoPart}/coverage/current") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val fullRepoName = "$orgPart/$repoPart"

        val projectName = call.request.queryParameters["project"]
        val branchName = call.request.queryParameters["branch"]

        val branchSearch = if (branchName != null) {
            BranchSearch(branchName = branchName)
        } else {
            BranchSearch(branchType = BranchType.MAINLINE)
        }

        val repositoryCurrentCoverage = repositoryCoverageService.fetchRepositoryCurrentCoverage(
            fullRepoName,
            projectName,
            branchSearch
        )

        repositoryCurrentCoverage?.let { call.respond(HttpStatusCode.OK, it) }
            ?: call.respond(HttpStatusCode.NoContent)
    }

    get("/api/v1/repo/{orgPart}/{repoPart}/tests/flaky") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val projectName = call.request.queryParameters["project"]
        val maxRuns = call.request.queryParameters["max_runs"]?.toInt() ?: 50
        val flakyThreshold = call.request.queryParameters["threshold"]?.toInt() ?: 5
        val branchType: BranchType = BranchType.valueOf(call.request.queryParameters["branch_type"] ?: "MAINLINE")

        val fullRepoName = "$orgPart/$repoPart"

        val flakyTests = repositoryTestRunService.fetchFlakyTests(
            repoName = fullRepoName,
            projectName = projectName,
            maxRuns = maxRuns,
            flakyFailureThreshold = flakyThreshold,
            branchType = branchType
        )

        if (flakyTests.isNotEmpty()) {
            call.respond(
                HttpStatusCode.OK,
                RepositoryFlakyTests(
                    tests = flakyTests,
                    maxRuns = maxRuns,
                    failureCountThreshold = flakyThreshold
                )
            )
        } else {
            call.respond(HttpStatusCode.NoContent)
        }
    }

    get("/api/v1/repo/{orgPart}/{repoPart}/tests/runs/summaries") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val projectName = call.request.queryParameters["project"]
        val limit = min(call.request.queryParameters["limit"]?.toInt() ?: 10, 100) // Defaults to 10 with a max of 100

        val fullRepoName = "$orgPart/$repoPart"

        val testRunSummaries = repositoryTestRunService.fetchRepositoryTestRunSummaries(fullRepoName, projectName, limit)

        call.respond(HttpStatusCode.OK, RepositoryTestRunSummaries(testRuns = testRunSummaries))
    }
}
