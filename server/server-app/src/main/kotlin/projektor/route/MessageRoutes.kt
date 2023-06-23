package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
import projektor.message.MessageService
import projektor.server.api.PublicId

fun Route.messages(messageService: MessageService) {
    get("/run/{publicId}/messages") {
        val publicId = call.parameters.getOrFail("publicId")

        call.respond(HttpStatusCode.OK, messageService.getTestRunMessages(PublicId(publicId)))
    }
}
