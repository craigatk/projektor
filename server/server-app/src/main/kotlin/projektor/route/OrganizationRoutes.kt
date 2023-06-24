package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
import projektor.organization.coverage.OrganizationCoverageService

fun Route.organization(organizationCoverageService: OrganizationCoverageService) {
    get("/org/{orgName}/coverage") {
        val orgName = call.parameters.getOrFail("orgName")

        val organizationCoverage = organizationCoverageService.getCoverage(orgName)

        if (organizationCoverage.repositories.isNotEmpty()) {
            call.respond(HttpStatusCode.OK, organizationCoverage)
        } else {
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
