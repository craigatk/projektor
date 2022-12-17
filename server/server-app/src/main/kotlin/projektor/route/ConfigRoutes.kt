package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
