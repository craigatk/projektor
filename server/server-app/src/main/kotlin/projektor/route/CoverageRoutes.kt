package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.util.getOrFail
import projektor.auth.AuthConfig
import projektor.auth.AuthService
import projektor.coverage.CoverageService
import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.route.CompressionRequest.receiveCompressedOrPlainTextPayload
import projektor.server.api.PublicId
import projektor.server.api.coverage.CoverageExists
import projektor.server.api.coverage.CoverageFiles
import projektor.server.api.coverage.SaveCoverageError

fun Route.coverage(authService: AuthService, coverageService: CoverageService) {
    post("/run/{publicId}/coverage") {
        val publicId = call.parameters.getOrFail("publicId")

        if (!authService.isAuthValid(call.request.header(AuthConfig.PublishToken))) {
            call.respond(HttpStatusCode.Unauthorized)
        } else {
            val reportXml = receiveCompressedOrPlainTextPayload(call)

            try {
                coverageService.saveReport(CoverageFilePayload(reportContents = reportXml), PublicId(publicId))
                    ?.let { call.respond(HttpStatusCode.OK) }
                    ?: call.respond(HttpStatusCode.BadRequest)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, SaveCoverageError(publicId, e.message))
            }
        }
    }

    post("/run/{publicId}/coverageFile") {
        val publicId = call.parameters.getOrFail("publicId")

        if (!authService.isAuthValid(call.request.header(AuthConfig.PublishToken))) {
            call.respond(HttpStatusCode.Unauthorized)
        } else {
            val incomingPayload = receiveCompressedOrPlainTextPayload(call)

            try {
                coverageService.parseAndSaveReport(incomingPayload, PublicId(publicId))
                    ?.let { call.respond(HttpStatusCode.OK) }
                    ?: call.respond(HttpStatusCode.BadRequest)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, SaveCoverageError(publicId, e.message))
            }
        }
    }

    get("/run/{publicId}/coverage") {
        val publicId = call.parameters.getOrFail("publicId")

        val coverage = coverageService.getCoverageWithPreviousRunComparison(PublicId(publicId))

        coverage
            ?.let { call.respond(HttpStatusCode.OK, it) }
            ?: call.respond(HttpStatusCode.NoContent)
    }

    get("/run/{publicId}/coverage/overall") {
        val publicId = call.parameters.getOrFail("publicId")

        val overallCoverageStats = coverageService.getOverallStats(PublicId(publicId))

        overallCoverageStats
            ?.let { call.respond(HttpStatusCode.OK, it) }
            ?: call.respond(HttpStatusCode.NoContent)
    }

    get("/run/{publicId}/coverage/exists") {
        val publicId = call.parameters.getOrFail("publicId")

        val coverageExists = coverageService.coverageExists(PublicId(publicId))

        call.respond(HttpStatusCode.OK, CoverageExists(coverageExists))
    }

    get("/run/{publicId}/coverage/{coverageGroupName}/files") {
        val publicId = call.parameters.getOrFail("publicId")
        val coverageGroupName = call.parameters.getOrFail("coverageGroupName")

        val coverageGroupFiles = coverageService.getCoverageGroupFiles(PublicId(publicId), coverageGroupName)

        if (coverageGroupFiles.isNotEmpty()) {
            call.respond(HttpStatusCode.OK, CoverageFiles(coverageGroupFiles))
        } else {
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
