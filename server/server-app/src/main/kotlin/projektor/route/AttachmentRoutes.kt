package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.request.receiveStream
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.util.getOrFail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import projektor.attachment.AddAttachmentResult
import projektor.attachment.AttachmentService
import projektor.auth.AuthConfig
import projektor.auth.AuthService
import projektor.server.api.PublicId
import projektor.server.api.attachments.AddAttachmentError
import projektor.server.api.attachments.AddAttachmentResponse
import projektor.server.api.attachments.Attachments

fun Route.attachments(
    attachmentService: AttachmentService?,
    authService: AuthService
) {
    post("/run/{publicId}/attachments/{attachmentName}") {
        val publicId = call.parameters.getOrFail("publicId")
        val attachmentName = call.parameters.getOrFail("attachmentName")
        val attachmentStream = withContext(Dispatchers.IO) {
            call.receiveStream()
        }

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
