package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
import projektor.repository.testrun.RepositoryTestRunService
import projektor.server.api.repository.BranchType
import projektor.server.api.repository.RepositoryFlakyTests

fun Route.repository(
    repositoryTestRunService: RepositoryTestRunService
) {
    get("/repo/{orgPart}/{repoPart}/timeline") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val fullRepoName = "$orgPart/$repoPart"

        val timeline = repositoryTestRunService.fetchRepositoryTestRunTimeline(fullRepoName, null)

        timeline?.let { call.respond(HttpStatusCode.OK, it) }
            ?: call.respond(HttpStatusCode.NoContent)
    }

    get("/repo/{orgPart}/{repoPart}/project/{projectName}/timeline") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val projectName = call.parameters.getOrFail("projectName")
        val fullRepoName = "$orgPart/$repoPart"

        val timeline = repositoryTestRunService.fetchRepositoryTestRunTimeline(fullRepoName, projectName)

        timeline?.let { call.respond(HttpStatusCode.OK, it) }
            ?: call.respond(HttpStatusCode.NoContent)
    }

    suspend fun handleFlakyTestsRequest(
        orgPart: String,
        repoPart: String,
        projectName: String?,
        maxRuns: Int,
        flakyThreshold: Int,
        branchType: BranchType,
        call: ApplicationCall
    ) {
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

    get("/repo/{orgPart}/{repoPart}/tests/flaky") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val maxRuns = call.request.queryParameters["max_runs"]?.toInt() ?: 50
        val flakyThreshold = call.request.queryParameters["threshold"]?.toInt() ?: 5
        val branchType: BranchType = BranchType.valueOf(call.request.queryParameters["branch_type"] ?: "ALL")

        handleFlakyTestsRequest(
            orgPart = orgPart,
            repoPart = repoPart,
            projectName = null,
            maxRuns = maxRuns,
            flakyThreshold = flakyThreshold,
            branchType = branchType,
            call
        )
    }

    get("/repo/{orgPart}/{repoPart}/project/{projectName}/tests/flaky") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val projectName = call.parameters.getOrFail("projectName")
        val maxRuns = call.request.queryParameters["max_runs"]?.toInt() ?: 50
        val flakyThreshold = call.request.queryParameters["threshold"]?.toInt() ?: 5
        val branchType: BranchType = BranchType.valueOf(call.request.queryParameters["branch_type"] ?: "ALL")

        handleFlakyTestsRequest(
            orgPart = orgPart,
            repoPart = repoPart,
            projectName = projectName,
            maxRuns = maxRuns,
            flakyThreshold = flakyThreshold,
            branchType = branchType,
            call
        )
    }
}
