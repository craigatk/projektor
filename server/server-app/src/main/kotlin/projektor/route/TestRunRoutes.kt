package projektor.route

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import projektor.server.api.PublicId
import projektor.testrun.TestRunService

@KtorExperimentalAPI
fun Route.testRuns(testRunService: TestRunService) {
    get("/run/{publicId}") {
        val publicId = call.parameters.getOrFail("publicId")

        val testRun = testRunService.fetchTestRun(PublicId(publicId))

        testRun?.let { call.respond(HttpStatusCode.OK, it) }
                ?: call.respond(HttpStatusCode.NotFound)
    }
    get("/run/{publicId}/summary") {
        val publicId = call.parameters.getOrFail("publicId")

        val testRunSummary = testRunService.fetchTestRunSummary(PublicId(publicId))

        testRunSummary?.let { call.respond(HttpStatusCode.OK, it) }
                ?: call.respond(HttpStatusCode.NotFound)
    }
}
