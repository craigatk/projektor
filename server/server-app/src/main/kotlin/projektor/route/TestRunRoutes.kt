package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
import projektor.server.api.PublicId
import projektor.testrun.TestRunService

fun Route.testRuns(testRunService: TestRunService) {
    get("/run/{publicId}") {
        val publicId = call.parameters.getOrFail("publicId")

        val testRun = testRunService.fetchTestRun(PublicId(publicId))

        testRun?.let {
            testRunService.addTestRunSummaryToSpan(testRun.summary)

            call.respond(HttpStatusCode.OK, it)
        } ?: call.respond(HttpStatusCode.NotFound)
    }
    get("/run/{publicId}/summary") {
        val publicId = call.parameters.getOrFail("publicId")

        val testRunSummary = testRunService.fetchTestRunSummary(PublicId(publicId))

        testRunSummary?.let {
            testRunService.addTestRunSummaryToSpan(testRunSummary)

            call.respond(HttpStatusCode.OK, it)
        } ?: call.respond(HttpStatusCode.NotFound)
    }
}
