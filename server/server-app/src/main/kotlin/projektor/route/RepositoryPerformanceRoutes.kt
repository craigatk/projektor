package projektor.route

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import projektor.repository.performance.RepositoryPerformanceService

fun Route.repositoryPerformance(repositoryPerformanceService: RepositoryPerformanceService) {
    get("/repo/{orgPart}/{repoPart}/performance/timeline") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val fullRepoName = "$orgPart/$repoPart"

        val repositoryPerformanceTimeline = repositoryPerformanceService.fetchPerformanceTimeline(fullRepoName, null)

        repositoryPerformanceTimeline?.let { call.respond(HttpStatusCode.OK, it) }
            ?: call.respond(HttpStatusCode.NoContent)
    }

    get("/repo/{orgPart}/{repoPart}/project/{projectName}/performance/timeline") {
        val orgPart = call.parameters.getOrFail("orgPart")
        val repoPart = call.parameters.getOrFail("repoPart")
        val projectName = call.parameters.getOrFail("projectName")
        val fullRepoName = "$orgPart/$repoPart"

        val repositoryPerformanceTimeline = repositoryPerformanceService.fetchPerformanceTimeline(fullRepoName, projectName)

        repositoryPerformanceTimeline?.let { call.respond(HttpStatusCode.OK, it) }
            ?: call.respond(HttpStatusCode.NoContent)
    }
}
