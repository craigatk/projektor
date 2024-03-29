package projektor.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import projektor.organization.coverage.OrganizationCoverageService
import projektor.repository.coverage.RepositoryCoverageService
import projektor.server.api.repository.BranchSearch

fun Route.api(
    organizationCoverageService: OrganizationCoverageService,
    repositoryCoverageService: RepositoryCoverageService,
) {
    get("/api/org/{orgName}/coverage/current") {
        val orgName = call.parameters.getOrFail("orgName")

        val organizationCoverage = organizationCoverageService.getCoverage(orgName)

        if (organizationCoverage.repositories.isNotEmpty()) {
            call.respond(HttpStatusCode.OK, organizationCoverage)
        } else {
            call.respond(HttpStatusCode.NoContent)
        }
    }

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
