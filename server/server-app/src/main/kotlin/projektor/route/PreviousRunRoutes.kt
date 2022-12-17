package projektor.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
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
