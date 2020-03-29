package projektor.cleanup

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
data class CleanupConfig(val maxReportAgeDays: Int?) {
    companion object {
        fun createCleanupConfig(applicationConfig: ApplicationConfig) = CleanupConfig(
                applicationConfig.propertyOrNull("ktor.cleanup.maxReportAgeDays")?.getString()?.toInt()
        )
    }
}
