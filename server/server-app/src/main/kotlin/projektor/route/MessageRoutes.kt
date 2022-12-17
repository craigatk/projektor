package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import projektor.message.MessageService
import projektor.server.api.PublicId

fun Route.messages(messageService: MessageService) {
    get("/run/{publicId}/messages") {
        val publicId = call.parameters.getOrFail("publicId")

        call.respond(HttpStatusCode.OK, messageService.getTestRunMessages(PublicId(publicId)))
    }
}
