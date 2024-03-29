package projektor.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import projektor.repository.coverage.RepositoryCoverageService
import projektor.server.api.repository.BranchSearch

fun Route.api(
    repositoryCoverageService: RepositoryCoverageService,
) {
    get("/api/repo/{orgPart}/{repoPart}/coverage/current") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val fullRepoName = "$orgPart/$repoPart"

        val projectName = call.request.queryParameters["project"]
        val branchName = call.request.queryParameters["branch"]

        val repositoryCurrentCoverage = repositoryCoverageService.fetchRepositoryCurrentCoverage(
            fullRepoName,
            projectName,
            BranchSearch(branchName = branchName)
        )

        repositoryCurrentCoverage?.let { call.respond(HttpStatusCode.OK, it) }
            ?: call.respond(HttpStatusCode.NoContent)
    }
}
