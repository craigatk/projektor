package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
import projektor.server.api.PublicId
import projektor.testsuite.TestSuiteSearchCriteria
import projektor.testsuite.TestSuiteService

fun Route.testSuites(testSuiteService: TestSuiteService) {
    get("/run/{publicId}/suite/{testSuiteIdx}/systemErr") {
        val publicId = call.parameters.getOrFail("publicId")
        val testSuiteIdx = call.parameters.getOrFail("testSuiteIdx").toInt()

        val testSuiteSystemErr = testSuiteService.fetchTestSuiteSystemErr(PublicId(publicId), testSuiteIdx)

        call.respond(HttpStatusCode.OK, testSuiteSystemErr)
    }
    get("/run/{publicId}/suite/{testSuiteIdx}/systemOut") {
        val publicId = call.parameters.getOrFail("publicId")
        val testSuiteIdx = call.parameters.getOrFail("testSuiteIdx").toInt()

        val testSuiteSystemOut = testSuiteService.fetchTestSuiteSystemOut(PublicId(publicId), testSuiteIdx)

        call.respond(HttpStatusCode.OK, testSuiteSystemOut)
    }
    get("/run/{publicId}/suite/{testSuiteIdx}") {
        val publicId = call.parameters.getOrFail("publicId")
        val testSuiteIdx = call.parameters.getOrFail("testSuiteIdx").toInt()

        val testSuite = testSuiteService.fetchTestSuite(PublicId(publicId), testSuiteIdx)

        testSuite?.let { call.respond(HttpStatusCode.OK, testSuite) }
            ?: call.respond(HttpStatusCode.NotFound)
    }
    get("/run/{publicId}/suites") {
        val publicId = call.parameters.getOrFail("publicId")

        val packageName = call.request.queryParameters["package"]
        val failedOnly = call.request.queryParameters["failed"]?.toBoolean() ?: false

        val testSuiteSearchCriteria = TestSuiteSearchCriteria(packageName, failedOnly)

        val testSuites = testSuiteService.fetchTestSuites(PublicId(publicId), testSuiteSearchCriteria)

        call.respond(HttpStatusCode.OK, testSuites)
    }
}
