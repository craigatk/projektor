package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import projektor.ai.AIConfig
import projektor.cleanup.CleanupConfig
import projektor.server.api.config.ServerCleanupConfig
import projektor.server.api.config.ServerConfig

fun Route.config(
    cleanupConfig: CleanupConfig,
    aiConfig: AIConfig,
) {
    get("/config") {
        call.respond(
            HttpStatusCode.OK,
            ServerConfig(
                ServerCleanupConfig(
                    cleanupConfig.enabled,
                    cleanupConfig.maxReportAgeDays,
                ),
                aiConfig.toServerConfig(),
            ),
        )
    }
}
