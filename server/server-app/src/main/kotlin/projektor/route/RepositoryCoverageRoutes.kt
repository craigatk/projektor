package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
import projektor.repository.coverage.RepositoryCoverageService
import projektor.server.api.coverage.CoverageExists
import projektor.server.api.repository.BranchSearch
import projektor.server.api.repository.BranchType

fun Route.repositoryCoverage(repositoryCoverageService: RepositoryCoverageService) {
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

    get("/repo/{orgPart}/{repoPart}/coverage/exists") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val fullRepoName = "$orgPart/$repoPart"

        val coverageExists = repositoryCoverageService.coverageExists(fullRepoName, null)

        call.respond(HttpStatusCode.OK, CoverageExists(coverageExists))
    }

    get("/repo/{orgPart}/{repoPart}/project/{projectName}/coverage/exists") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val projectName = call.parameters.getOrFail("projectName")
        val fullRepoName = "$orgPart/$repoPart"

        val coverageExists = repositoryCoverageService.coverageExists(fullRepoName, projectName)

        call.respond(HttpStatusCode.OK, CoverageExists(coverageExists))
    }

    suspend fun handleCurrentCoverageRequest(
        orgPart: String,
        repoPart: String,
        projectName: String?,
        call: ApplicationCall,
    ) {
        val fullRepoName = "$orgPart/$repoPart"

        val repositoryCurrentCoverage =
            repositoryCoverageService.fetchRepositoryCurrentCoverage(
                fullRepoName,
                projectName,
                BranchSearch(branchType = BranchType.MAINLINE),
            )

        if (repositoryCurrentCoverage != null) {
            call.respond(
                HttpStatusCode.OK,
                repositoryCurrentCoverage,
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
