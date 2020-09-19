package projektor.route

import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.Route

fun Route.ui() {
    static("/") {
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
