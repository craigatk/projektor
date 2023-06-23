package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageService
import projektor.repository.coverage.RepositoryCoverageService
import projektor.server.api.repository.BranchType
import projektor.server.api.repository.coverage.RepositoryCurrentCoverage

fun Route.repositoryCoverage(
    coverageService: CoverageService,
    previousTestRunService: PreviousTestRunService,
    repositoryCoverageService: RepositoryCoverageService,
) {
    fun findBranchType(call: ApplicationCall): BranchType {
        val branchTypeString = (call.request.queryParameters["branch"] ?: "MAINLINE").uppercase()
        return BranchType.valueOf(branchTypeString)
    }

    get("/repo/{orgPart}/{repoPart}/coverage/timeline") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val fullRepoName = "$orgPart/$repoPart"
        val branchType = findBranchType(call)

        val coverageTimeline = repositoryCoverageService.fetchRepositoryCoverageTimeline(branchType, fullRepoName, null)

        coverageTimeline?.let { call.respond(HttpStatusCode.OK, it) }
            ?: call.respond(HttpStatusCode.NoContent)
    }

    get("/repo/{orgPart}/{repoPart}/project/{projectName}/coverage/timeline") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val fullReposName = "$orgPart/$repoPart"
        val projectName = call.parameters.getOrFail("projectName")
        val branchType = findBranchType(call)

        val coverageTimeline = repositoryCoverageService.fetchRepositoryCoverageTimeline(branchType, fullReposName, projectName)

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

        val mostRecentTestRun = previousTestRunService.findMostRecentRunWithCoverage(BranchType.MAINLINE, fullRepoName, projectName)
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
