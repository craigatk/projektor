package projektor.route

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.util.getOrFail
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
