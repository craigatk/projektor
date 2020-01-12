package projektor.route

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import projektor.incomingresults.GroupedTestResultsService
import projektor.incomingresults.TestResultsProcessingService
import projektor.incomingresults.TestResultsService
import projektor.server.api.PublicId
import projektor.server.api.SaveResultsResponse

@KtorExperimentalAPI
fun Route.results(
    testResultsService: TestResultsService,
    groupedTestResultsService: GroupedTestResultsService,
    testResultsProcessingService: TestResultsProcessingService
) {
    post("/results") {
        val resultsBlob = call.receive<String>()

        if (resultsBlob.isNotBlank()) {
            val publicId = testResultsService.persistTestResultsAsync(resultsBlob)

            call.respond(HttpStatusCode.OK, SaveResultsResponse(publicId.id, "/tests/${publicId.id}"))
        } else {
            call.respond(HttpStatusCode.BadRequest)
        }
    }
    post("/groupedResults") {
        val groupedResultsBlob = call.receive<String>()

        if (groupedResultsBlob.isNotBlank()) {
            val publicId = groupedTestResultsService.persistTestResultsAsync(groupedResultsBlob)

            call.respond(HttpStatusCode.OK, SaveResultsResponse(publicId.id, "/tests/${publicId.id}"))
        } else {
            call.respond(HttpStatusCode.BadRequest)
        }
    }
    get("/results/{publicId}/status") {
        val publicId = call.parameters.getOrFail("publicId")

        val processingResults = testResultsProcessingService.fetchResultsProcessing(PublicId(publicId))

        processingResults
                ?.let { call.respond(HttpStatusCode.OK, it) }
                ?: call.respond(HttpStatusCode.NotFound)
    }
}
