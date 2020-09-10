package projektor.route

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import projektor.organization.coverage.OrganizationCoverageService

@KtorExperimentalAPI
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
