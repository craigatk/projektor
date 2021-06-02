package projektor.route

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.util.getOrFail
import projektor.message.MessageService
import projektor.server.api.PublicId

fun Route.messages(messageService: MessageService) {
    get("/run/{publicId}/messages") {
        val publicId = call.parameters.getOrFail("publicId")

        call.respond(HttpStatusCode.OK, messageService.getTestRunMessages(PublicId(publicId)))
    }
}
