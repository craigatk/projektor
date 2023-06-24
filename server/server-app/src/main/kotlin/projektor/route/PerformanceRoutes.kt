package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
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
