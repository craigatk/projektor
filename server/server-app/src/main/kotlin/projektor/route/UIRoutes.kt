package projektor.route

import io.ktor.server.http.content.defaultResource
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.routing.Route

fun Route.ui() {
    static("/") {
        resources("static/")
        defaultResource("static/index.html")
    }
    static("/admin/{path...}") {
        resources("static/")
        defaultResource("static/index.html")
    }
    static("/organization/{path...}") {
        resources("static/")
        defaultResource("static/index.html")
    }
    static("/repository/{path...}") {
        resources("static/")
        defaultResource("static/index.html")
    }
    static("/tests/{path...}") {
        resources("static/")
        defaultResource("static/index.html")
    }
}
