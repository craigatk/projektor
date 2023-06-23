package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import projektor.error.ProcessingFailureService

fun Route.failure(processingFailureService: ProcessingFailureService) {
    get("/failures/recent") {
        val count = (call.request.queryParameters["count"] ?: "5").toInt()

        val failures = processingFailureService.fetchRecentProcessingFailures(count)

        call.respond(HttpStatusCode.OK, failures)
    }
}
