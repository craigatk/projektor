package projektor.route

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import projektor.badge.TestRunBadgeService
import projektor.server.api.PublicId

fun Route.badgeTests(testRunBadgeService: TestRunBadgeService) {
    get("/run/{publicId}/badge/tests") {
        val publicId = call.parameters.getOrFail("publicId")

        val svgBadge = testRunBadgeService.createTestsBadge(PublicId(publicId))

        respondWithSvg(svgBadge, call)
    }

    get("/repo/{orgPart}/{repoPart}/badge/tests") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val fullRepoName = "$orgPart/$repoPart"

        val svgBadge = testRunBadgeService.createTestsBadge(fullRepoName, null)

        respondWithSvg(svgBadge, call)
    }

    get("/repo/{orgPart}/{repoPart}/project/{projectName}/badge/tests") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val projectName = call.parameters.getOrFail("projectName")
        val fullRepoName = "$orgPart/$repoPart"

        val svgBadge = testRunBadgeService.createTestsBadge(fullRepoName, projectName)

        respondWithSvg(svgBadge, call)
    }
}
