package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import projektor.server.api.PublicId
import projektor.testrun.attributes.TestRunSystemAttributesService

fun Route.testRunSystemAttributes(testRunSystemAttributesService: TestRunSystemAttributesService) {
    get("/run/{publicId}/attributes") {
        val publicId = call.parameters.getOrFail("publicId")

        val systemAttributes = testRunSystemAttributesService.fetchAttributes(PublicId(publicId))

        systemAttributes?.let { call.respond(HttpStatusCode.OK, it) }
            ?: call.respond(HttpStatusCode.NotFound)
    }

    post("/run/{publicId}/attributes/pin") {
        val publicId = call.parameters.getOrFail("publicId")

        val rowsAffected = testRunSystemAttributesService.pin(PublicId(publicId))

        if (rowsAffected > 0) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.NotFound)
    }

    post("/run/{publicId}/attributes/unpin") {
        val publicId = call.parameters.getOrFail("publicId")

        val rowsAffected = testRunSystemAttributesService.unpin(PublicId(publicId))

        if (rowsAffected > 0) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.NotFound)
    }
}
