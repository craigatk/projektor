package projektor.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import projektor.performance.PerformanceResultsService
import projektor.server.api.PublicId
import projektor.server.api.performance.PerformanceResults

fun Route.performance(performanceResultsService: PerformanceResultsService) {
    get("/run/{publicId}/performance") {
        val publicId = call.parameters.getOrFail("publicId")

        val performanceResults = performanceResultsService.fetchResults(PublicId(publicId))

        if (performanceResults.isNotEmpty()) {
            call.respond(HttpStatusCode.OK, PerformanceResults(performanceResults))
        } else {
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
