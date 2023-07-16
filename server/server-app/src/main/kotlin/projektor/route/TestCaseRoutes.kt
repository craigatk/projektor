package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
import projektor.server.api.PublicId
import projektor.testcase.TestCaseService

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
    get("/run/{publicId}/suite/{testSuiteIdx}/case/{testCaseIdx}/systemErr") {
        val publicId = call.parameters.getOrFail("publicId")
        val testSuiteIdx = call.parameters.getOrFail("testSuiteIdx").toInt()
        val testCaseIdx = call.parameters.getOrFail("testCaseIdx").toInt()

        val systemErr = testCaseService.fetchTestCaseSystemErr(PublicId(publicId), testSuiteIdx, testCaseIdx)

        call.respond(HttpStatusCode.OK, systemErr)
    }
    get("/run/{publicId}/suite/{testSuiteIdx}/case/{testCaseIdx}/systemOut") {
        val publicId = call.parameters.getOrFail("publicId")
        val testSuiteIdx = call.parameters.getOrFail("testSuiteIdx").toInt()
        val testCaseIdx = call.parameters.getOrFail("testCaseIdx").toInt()

        val systemOut = testCaseService.fetchTestCaseSystemOut(PublicId(publicId), testSuiteIdx, testCaseIdx)

        call.respond(HttpStatusCode.OK, systemOut)
    }
}
