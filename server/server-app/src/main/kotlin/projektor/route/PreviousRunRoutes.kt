package projektor.route

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import projektor.compare.PreviousTestRunService
import projektor.server.api.PublicId

@KtorExperimentalAPI
fun Route.previousRuns(previousTestRunService: PreviousTestRunService) {
    get("/run/{publicId}/previous") {
        val publicId = call.parameters.getOrFail("publicId")

        val previousPublicId = previousTestRunService.findPreviousMainBranchRunWithCoverage(PublicId(publicId))

        previousPublicId?.let { call.respond(HttpStatusCode.OK, previousPublicId) }
            ?: call.respond(HttpStatusCode.NoContent)
    }
}
