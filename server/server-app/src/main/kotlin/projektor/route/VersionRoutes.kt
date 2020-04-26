package projektor.route

import io.ktor.http.content.defaultResource
import io.ktor.http.content.static
import io.ktor.routing.Route

fun Route.version() {
    static("/version") {
        defaultResource("version.json", "/")
    }
}
