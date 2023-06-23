package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
import projektor.compare.PreviousTestRunService
import projektor.server.api.PublicId

fun Route.previousRuns(previousTestRunService: PreviousTestRunService) {
    get("/run/{publicId}/previous") {
        val publicId = call.parameters.getOrFail("publicId")

        val previousPublicId = previousTestRunService.findPreviousMainBranchRunWithCoverage(PublicId(publicId))

        previousPublicId?.let { call.respond(HttpStatusCode.OK, previousPublicId) }
            ?: call.respond(HttpStatusCode.NoContent)
    }
}
