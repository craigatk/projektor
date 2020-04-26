package projektor.route

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import projektor.attachment.AddAttachmentResult
import projektor.attachment.AttachmentService
import projektor.auth.AuthConfig
import projektor.auth.AuthService
import projektor.server.api.PublicId
import projektor.server.api.attachments.AddAttachmentError
import projektor.server.api.attachments.AddAttachmentResponse
import projektor.server.api.attachments.Attachments

@KtorExperimentalAPI
fun Route.attachments(
    attachmentService: AttachmentService?,
    authService: AuthService
) {
    post("/run/{publicId}/attachments/{attachmentName}") {
        val publicId = call.parameters.getOrFail("publicId")
        val attachmentName = call.parameters.getOrFail("attachmentName")
        val attachmentStream = call.receiveStream()

        val contentLengthInBytes = call.request.header("content-length")?.toLong()

        if (!authService.isAuthValid(call.request.header(AuthConfig.PublishToken))) {
            call.respond(HttpStatusCode.Unauthorized)
        } else if (attachmentService != null) {
            if (attachmentService.attachmentSizeValid(contentLengthInBytes)) {
                val addAttachmentResult = attachmentService.addAttachment(PublicId(publicId), attachmentName, attachmentStream, contentLengthInBytes)

                when (addAttachmentResult) {
                    AddAttachmentResult.Success -> call.respond(HttpStatusCode.OK, AddAttachmentResponse(attachmentName, null))
                    is AddAttachmentResult.Failure -> call.respond(HttpStatusCode.BadRequest, AddAttachmentResponse(attachmentName, AddAttachmentError.ATTACHMENT_FAILED))
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, AddAttachmentResponse(null, AddAttachmentError.ATTACHMENT_TOO_LARGE))
            }
        } else {
            call.respond(HttpStatusCode.BadRequest, AddAttachmentResponse(null, AddAttachmentError.ATTACHMENTS_DISABLED))
        }
    }

    get("/run/{publicId}/attachments/{attachmentName}") {
        val publicId = call.parameters.getOrFail("publicId")
        val attachmentName = call.parameters.getOrFail("attachmentName")

        if (attachmentService != null) {
            val attachment = attachmentService.getAttachment(PublicId(publicId), attachmentName)

            if (attachment != null) {
                call.respondBytes(attachment.readBytes())
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        } else {
            call.respond(HttpStatusCode.BadRequest)
        }
    }

    get("/run/{publicId}/attachments") {
        val publicId = call.parameters.getOrFail("publicId")

        if (attachmentService != null) {
            val attachments = attachmentService.listAttachments(PublicId(publicId))

            call.respond(HttpStatusCode.OK, Attachments(attachments))
        } else {
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}
