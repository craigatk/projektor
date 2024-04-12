package projektor.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun respondWithSvg(
    svgBadge: String?,
    call: ApplicationCall,
) {
    svgBadge?.let { svg ->
        call.respondText(
            svg,
            ContentType.Image.SVG,
            HttpStatusCode.OK,
        )
    } ?: call.respond(HttpStatusCode.NotFound)
}
