package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
import projektor.metadata.TestRunMetadataService
import projektor.server.api.PublicId
import projektor.versioncontrol.VersionControlConfig

fun Route.metadata(
    testRunMetadataService: TestRunMetadataService,
    versionControlConfig: VersionControlConfig
) {
    get("/run/{publicId}/metadata") {
        val publicId = call.parameters.getOrFail("publicId")

        val metadata = testRunMetadataService.fetchResultsMetadata(PublicId(publicId))

        metadata?.let { call.respond(HttpStatusCode.OK, it) }
            ?: call.respond(HttpStatusCode.NoContent)
    }

    get("/run/{publicId}/metadata/git") {
        val publicId = call.parameters.getOrFail("publicId")

        val gitMetadata = testRunMetadataService.fetchGitMetadata(PublicId(publicId))

        if (gitMetadata != null) {
            gitMetadata.gitHubBaseUrl = versionControlConfig.gitHubBaseUrl

            call.respond(HttpStatusCode.OK, gitMetadata)
        } else {
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
