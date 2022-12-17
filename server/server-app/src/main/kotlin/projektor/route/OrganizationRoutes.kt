package projektor.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.*
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
