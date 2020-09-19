package projektor.route

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import projektor.repository.coverage.RepositoryCoverageService

@KtorExperimentalAPI
fun Route.repository(repositoryCoverageService: RepositoryCoverageService) {
    get("/repo/{orgName}/{repoName}/coverage/timeline") {
        val orgName = call.parameters.getOrFail("orgName")
        val repoName = call.parameters.getOrFail("repoName")
        val fullReposName = "$orgName/$repoName"

        val coverageTimeline = repositoryCoverageService.fetchRepositoryCoverageTimeline(fullReposName, null)

        coverageTimeline?.let { call.respond(HttpStatusCode.OK, it) }
                ?: call.respond(HttpStatusCode.NoContent)
    }

    get("/repo/{orgName}/{repoName}/project/{projectName}/coverage/timeline") {
        val orgName = call.parameters.getOrFail("orgName")
        val repoName = call.parameters.getOrFail("repoName")
        val fullReposName = "$orgName/$repoName"
        val projectName = call.parameters.getOrFail("projectName")

        val coverageTimeline = repositoryCoverageService.fetchRepositoryCoverageTimeline(fullReposName, projectName)

        coverageTimeline?.let { call.respond(HttpStatusCode.OK, it) }
                ?: call.respond(HttpStatusCode.NoContent)
    }
}
