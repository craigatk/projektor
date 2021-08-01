package projektor.route

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageService
import projektor.repository.coverage.RepositoryCoverageService
import projektor.server.api.repository.coverage.RepositoryCurrentCoverage

fun Route.repositoryCoverage(
    coverageService: CoverageService,
    previousTestRunService: PreviousTestRunService,
    repositoryCoverageService: RepositoryCoverageService,
) {
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

    suspend fun handleCurrentCoverageRequest(
        orgPart: String,
        repoPart: String,
        projectName: String?,
        call: ApplicationCall
    ) {
        val fullRepoName = "$orgPart/$repoPart"

        val mostRecentTestRun = previousTestRunService.findMostRecentMainBranchRunWithCoverage(fullRepoName, projectName)
        val coveredPercentage = mostRecentTestRun?.publicId?.let { publicId -> coverageService.getCoveredLinePercentage(publicId) }

        if (mostRecentTestRun != null && coveredPercentage != null) {
            call.respond(
                HttpStatusCode.OK,
                RepositoryCurrentCoverage(
                    id = mostRecentTestRun.publicId.id,
                    coveredPercentage = coveredPercentage,
                    createdTimestamp = mostRecentTestRun.createdTimestamp
                )
            )
        } else {
            call.respond(HttpStatusCode.NoContent)
        }
    }

    get("/repo/{orgPart}/{repoPart}/coverage/current") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")

        handleCurrentCoverageRequest(orgPart, repoPart, null, call)
    }

    get("/repo/{orgPart}/{repoPart}/project/{projectName}/coverage/current") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val projectName = call.parameters.getOrFail("projectName")

        handleCurrentCoverageRequest(orgPart, repoPart, projectName, call)
    }
}