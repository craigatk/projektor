package projektor.route

import io.ktor.server.http.content.defaultResource
import io.ktor.server.http.content.static
import io.ktor.server.routing.Route

fun Route.version() {
    static("/version") {
        defaultResource("version.json", "/")
    }
}
