package projektor.route

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import projektor.error.ProcessingFailureService

fun Route.failure(processingFailureService: ProcessingFailureService) {
    get("/failures/recent") {
        val count = (call.request.queryParameters["count"] ?: "5").toInt()

        val failures = processingFailureService.fetchRecentProcessingFailures(count)

        call.respond(HttpStatusCode.OK, failures)
    }
}
