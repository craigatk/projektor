package projektor.route

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import projektor.metadata.TestRunMetadataService
import projektor.server.api.PublicId

@KtorExperimentalAPI
fun Route.metadata(testRunMetadataService: TestRunMetadataService) {
    get("/run/{publicId}/metadata") {
        val publicId = call.parameters.getOrFail("publicId")

        val metadata = testRunMetadataService.fetchResultsMetadata(PublicId(publicId))

        metadata?.let { call.respond(HttpStatusCode.OK, it) }
            ?: call.respond(HttpStatusCode.NoContent)
    }

    get("/run/{publicId}/metadata/git") {
        val publicId = call.parameters.getOrFail("publicId")

        val gitMetadata = testRunMetadataService.fetchGitMetadata(PublicId(publicId))

        gitMetadata?.let { call.respond(HttpStatusCode.OK, it) }
            ?: call.respond(HttpStatusCode.NoContent)
    }
}
