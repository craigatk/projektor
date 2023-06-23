package projektor.route

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
import projektor.badge.CoverageBadgeService
import projektor.server.api.PublicId

fun Route.badge(coverageBadgeService: CoverageBadgeService) {
    get("/run/{publicId}/badge/coverage") {
        val publicId = call.parameters.getOrFail("publicId")

        val svgBadge = coverageBadgeService.createCoverageBadge(PublicId(publicId))

        respondWithSvg(svgBadge, call)
    }

    get("/repo/{orgPart}/{repoPart}/badge/coverage") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val fullRepoName = "$orgPart/$repoPart"

        val svgBadge = coverageBadgeService.createCoverageBadge(fullRepoName, null)

        respondWithSvg(svgBadge, call)
    }

    get("/repo/{orgPart}/{repoPart}/project/{projectName}/badge/coverage") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val projectName = call.parameters.getOrFail("projectName")
        val fullRepoName = "$orgPart/$repoPart"

        val svgBadge = coverageBadgeService.createCoverageBadge(fullRepoName, projectName)

        respondWithSvg(svgBadge, call)
    }
}

private suspend fun respondWithSvg(svgBadge: String?, call: ApplicationCall) {
    svgBadge?.let { svg ->
        call.respondText(
            svg,
            ContentType.Image.SVG,
            HttpStatusCode.OK,
        )
    } ?: call.respond(HttpStatusCode.NotFound)
}
