package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.response.respond

private val validPublicId = Regex("[A-Za-z0-9]{1,64}")

/**
 * Responds with not found to any route whose publicId path parameter can't be a test run's public ID. Public IDs
 * become part of object store keys, and Ktor decodes path parameters, so otherwise a %2F in the URL would let a
 * request address keys outside the test run.
 */
val PublicIdValidation =
    createRouteScopedPlugin("PublicIdValidation") {
        onCall { call ->
            val publicId = call.parameters["publicId"]

            if (publicId != null && !validPublicId.matches(publicId)) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
