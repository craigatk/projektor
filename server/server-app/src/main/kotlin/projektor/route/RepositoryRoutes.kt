package projektor.route

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import projektor.repository.coverage.RepositoryCoverageService
import projektor.repository.testrun.RepositoryTestRunService
import projektor.server.api.repository.RepositoryFlakyTests

@KtorExperimentalAPI
fun Route.repository(
    repositoryCoverageService: RepositoryCoverageService,
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

    get("/repo/{orgPart}/{repoPart}/coverage/timeline") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val fullRepoName = "$orgPart/$repoPart"

        val coverageTimeline = repositoryCoverageService.fetchRepositoryCoverageTimeline(fullRepoName, null)

        coverageTimeline?.let { call.respond(HttpStatusCode.OK, it) }
            ?: call.respond(HttpStatusCode.NoContent)
    }

    get("/repo/{orgPart}/{repoPart}/project/{projectName}/coverage/timeline") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val fullReposName = "$orgPart/$repoPart"
        val projectName = call.parameters.getOrFail("projectName")

        val coverageTimeline = repositoryCoverageService.fetchRepositoryCoverageTimeline(fullReposName, projectName)

        coverageTimeline?.let { call.respond(HttpStatusCode.OK, it) }
            ?: call.respond(HttpStatusCode.NoContent)
    }

    suspend fun handleFlakyTestsRequest(
        orgPart: String,
        repoPart: String,
        projectName: String?,
        maxRuns: Int,
        flakyThreshold: Int,
        call: ApplicationCall
    ) {
        val fullRepoName = "$orgPart/$repoPart"

        val flakyTests = repositoryTestRunService.fetchFlakyTests(
            repoName = fullRepoName,
            projectName = projectName,
            maxRuns = maxRuns,
            flakyFailureThreshold = flakyThreshold
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

        handleFlakyTestsRequest(orgPart = orgPart, repoPart = repoPart, projectName = null, maxRuns = maxRuns, flakyThreshold = flakyThreshold, call)
    }

    get("/repo/{orgPart}/{repoPart}/project/{projectName}/tests/flaky") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val projectName = call.parameters.getOrFail("projectName")
        val maxRuns = call.request.queryParameters["max_runs"]?.toInt() ?: 50
        val flakyThreshold = call.request.queryParameters["threshold"]?.toInt() ?: 5

        handleFlakyTestsRequest(orgPart = orgPart, repoPart = repoPart, projectName = projectName, maxRuns = maxRuns, flakyThreshold = flakyThreshold, call)
    }
}
