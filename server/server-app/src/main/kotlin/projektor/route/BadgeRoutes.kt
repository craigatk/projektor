package projektor.route

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import projektor.badge.RepositoryCoverageBadgeService

@KtorExperimentalAPI
fun Route.badge(repositoryCoverageBadgeService: RepositoryCoverageBadgeService) {
    get("/repo/{orgPart}/{repoPart}/badge/coverage") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val fullRepoName = "$orgPart/$repoPart"

        val svgBadge = repositoryCoverageBadgeService.createCoverageBadge(fullRepoName, null)

        svgBadge?.let { svg ->
            call.respondText(
                svg,
                ContentType.Image.SVG,
                HttpStatusCode.OK,
            )
        } ?: call.respond(HttpStatusCode.NotFound)
    }

    get("/repo/{orgPart}/{repoPart}/project/{projectName}/badge/coverage") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val projectName = call.parameters.getOrFail("projectName")
        val fullRepoName = "$orgPart/$repoPart"

        val svgBadge = repositoryCoverageBadgeService.createCoverageBadge(fullRepoName, projectName)

        svgBadge?.let { svg ->
            call.respondText(
                svg,
                ContentType.Image.SVG,
                HttpStatusCode.OK,
            )
        } ?: call.respond(HttpStatusCode.NotFound)
    }
}
