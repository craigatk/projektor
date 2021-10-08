package projektor.route

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
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
