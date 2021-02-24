package projektor.route

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import projektor.auth.AuthConfig
import projektor.auth.AuthService
import projektor.incomingresults.GroupedTestResultsService
import projektor.incomingresults.PersistTestResultsException
import projektor.incomingresults.TestResultsProcessingService
import projektor.incomingresults.TestResultsService
import projektor.metrics.MetricsService
import projektor.route.CompressionRequest.receiveCompressedOrPlainTextPayload
import projektor.server.api.PublicId
import projektor.server.api.results.SaveResultsError
import projektor.server.api.results.SaveResultsResponse

@KtorExperimentalAPI
fun Route.results(
    testResultsService: TestResultsService,
    groupedTestResultsService: GroupedTestResultsService,
    testResultsProcessingService: TestResultsProcessingService,
    authService: AuthService,
    metricRegistry: MeterRegistry,
    metricsService: MetricsService
) {
    post("/results") {
        if (!authService.isAuthValid(call.request.header(AuthConfig.PublishToken))) {
            call.respond(HttpStatusCode.Unauthorized)
        } else {
            metricsService.incrementResultsProcessStartCounter()

            val resultsBlob = receiveCompressedOrPlainTextPayload(call)

            if (resultsBlob.isNotBlank()) {
                val publicId = testResultsService.persistTestResultsAsync(resultsBlob)

                call.respond(HttpStatusCode.OK, SaveResultsResponse(publicId.id, "/tests/${publicId.id}"))
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
    post("/groupedResults") {
        if (!authService.isAuthValid(call.request.header(AuthConfig.PublishToken))) {
            call.respond(HttpStatusCode.Unauthorized)
        } else {
            metricsService.incrementResultsProcessStartCounter()

            val timer = metricRegistry.timer("receive_grouped_results")
            val sample = Timer.start(metricRegistry)
            val groupedResultsBlob = receiveCompressedOrPlainTextPayload(call)
            sample.stop(timer)

            if (groupedResultsBlob.isNotBlank()) {
                try {
                    val publicId = groupedTestResultsService.persistTestResultsAsync(groupedResultsBlob)

                    call.respond(HttpStatusCode.OK, SaveResultsResponse(publicId.id, "/tests/${publicId.id}"))
                } catch (e: PersistTestResultsException) {
                    call.respond(HttpStatusCode.BadRequest, SaveResultsError(e.publicId.id, e.errorMessage))
                }
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
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
