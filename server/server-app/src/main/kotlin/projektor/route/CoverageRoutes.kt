package projektor.route

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import projektor.auth.AuthConfig
import projektor.auth.AuthService
import projektor.coverage.CoverageService
import projektor.server.api.PublicId

@KtorExperimentalAPI
fun Route.coverage(authService: AuthService, coverageService: CoverageService) {
    post("/run/{publicId}/coverage") {
        val publicId = call.parameters.getOrFail("publicId")

        if (!authService.isAuthValid(call.request.header(AuthConfig.PublishToken))) {
            call.respond(HttpStatusCode.Unauthorized)
        } else {
            val reportXml = call.receive<String>()

            coverageService.saveReport(reportXml, PublicId(publicId))

            call.respond(HttpStatusCode.OK)
        }
    }

    get("/run/{publicId}/coverage/overall") {
        val publicId = call.parameters.getOrFail("publicId")

        val overallCoverageStats = coverageService.getOverallStats(PublicId(publicId))

        overallCoverageStats
                ?.let { call.respond(HttpStatusCode.OK, it) }
                ?: call.respond(HttpStatusCode.NotFound)
    }
}
