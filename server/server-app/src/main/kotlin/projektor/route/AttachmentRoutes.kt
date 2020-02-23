package projektor.route

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import projektor.attachment.AttachmentStoreService
import projektor.attachment.CreateAttachmentResponse
import projektor.server.api.PublicId

@KtorExperimentalAPI
fun Route.attachments(attachmentStoreService: AttachmentStoreService?) {
    post("/run/{publicId}/attachment/{attachmentName}") {
        val publicId = call.parameters.getOrFail("publicId")
        val attachmentName = call.parameters.getOrFail("attachmentName")
        val attachmentStream = call.receiveStream()

        if (attachmentStoreService != null) {
            attachmentStoreService.conditionallyCreateBucketIfNotExists()

            attachmentStoreService.addAttachment(PublicId(publicId), attachmentName, attachmentStream)

            call.respond(HttpStatusCode.OK, CreateAttachmentResponse(true, true, attachmentName))
        } else {
            call.respond(HttpStatusCode.BadRequest, CreateAttachmentResponse(false, false, null))
        }
    }
}
