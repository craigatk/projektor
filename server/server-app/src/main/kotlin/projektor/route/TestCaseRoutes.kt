package projektor.route

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import projektor.server.api.PublicId
import projektor.testcase.TestCaseService

@KtorExperimentalAPI
fun Route.testCases(testCaseService: TestCaseService) {
    get("/run/{publicId}/suite/{testSuiteIdx}/case/{testCaseIdx}") {
        val publicId = call.parameters.getOrFail("publicId")
        val testSuiteIdx = call.parameters.getOrFail("testSuiteIdx").toInt()
        val testCaseIdx = call.parameters.getOrFail("testCaseIdx").toInt()

        val testCase = testCaseService.fetchTestCase(PublicId(publicId), testSuiteIdx, testCaseIdx)

        testCase?.let { call.respond(HttpStatusCode.OK, testCase) }
                ?: call.respond(HttpStatusCode.NotFound)
    }
    get("/run/{publicId}/cases/failed") {
        val publicId = call.parameters.getOrFail("publicId")

        val testCases = testCaseService.fetchFailedTestCases(PublicId(publicId))

        call.respond(HttpStatusCode.OK, testCases)
    }
    get("/run/{publicId}/cases/slow") {
        val publicId = call.parameters.getOrFail("publicId")

        val testCases = testCaseService.fetchSlowTestCases(PublicId(publicId), 10)

        call.respond(HttpStatusCode.OK, testCases)
    }
}
