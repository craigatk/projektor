package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.health() {
    get("/health") {
        call.respond(HttpStatusCode.OK, Status("OK"))
    }
}
