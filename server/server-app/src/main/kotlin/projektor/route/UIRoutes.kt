package projektor.route

import io.ktor.server.http.content.*
import io.ktor.server.routing.*

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
