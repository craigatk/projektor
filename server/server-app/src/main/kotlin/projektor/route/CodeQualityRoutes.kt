package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
import projektor.quality.CodeQualityReportRepository
import projektor.server.api.PublicId
import projektor.server.api.quality.CodeQualityReports

fun Route.codeQuality(
    codeQualityReportRepository: CodeQualityReportRepository
) {
    get("/run/{publicId}/quality") {
        val publicId = call.parameters.getOrFail("publicId")

        val codeQualityReports = codeQualityReportRepository.fetchCodeQualityReports(PublicId(publicId))

        if (codeQualityReports.isNotEmpty()) {
            call.respond(HttpStatusCode.OK, CodeQualityReports(reports = codeQualityReports))
        } else {
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
