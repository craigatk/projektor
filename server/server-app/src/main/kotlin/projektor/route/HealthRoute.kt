package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.health() {
    get("/health") {
        call.respond(HttpStatusCode.OK, Status("OK"))
    }
}
