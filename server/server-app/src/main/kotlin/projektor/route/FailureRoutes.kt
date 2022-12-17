package projektor.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import projektor.error.ProcessingFailureService

fun Route.failure(processingFailureService: ProcessingFailureService) {
    get("/failures/recent") {
        val count = (call.request.queryParameters["count"] ?: "5").toInt()

        val failures = processingFailureService.fetchRecentProcessingFailures(count)

        call.respond(HttpStatusCode.OK, failures)
    }
}
