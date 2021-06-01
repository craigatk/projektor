package projektor.route

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import projektor.cleanup.CleanupConfig
import projektor.server.api.config.ServerCleanupConfig
import projektor.server.api.config.ServerConfig

fun Route.config(cleanupConfig: CleanupConfig) {
    get("/config") {
        call.respond(
            HttpStatusCode.OK,
            ServerConfig(
                ServerCleanupConfig(
                    cleanupConfig.enabled,
                    cleanupConfig.maxReportAgeDays
                )
            )
        )
    }
}
