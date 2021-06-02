package projektor.route

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
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
