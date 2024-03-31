package projektor.route

import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
import projektor.badge.CoverageBadgeService
import projektor.server.api.PublicId

fun Route.badgeCoverage(coverageBadgeService: CoverageBadgeService) {
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
