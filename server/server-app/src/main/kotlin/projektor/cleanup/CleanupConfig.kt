package projektor.cleanup

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
data class CleanupConfig(val enabled: Boolean, val maxReportAgeDays: Int?, val dryRun: Boolean) {
    companion object {
        fun createCleanupConfig(applicationConfig: ApplicationConfig): CleanupConfig {
            val maxAgeDays = applicationConfig.propertyOrNull("ktor.cleanup.maxReportAgeDays")?.getString()?.toInt()
            val dryRun = applicationConfig.propertyOrNull("ktor.cleanup.dryRun")?.getString()?.toBoolean() ?: false

            return CleanupConfig(
                maxAgeDays != null && maxAgeDays > 0,
                maxAgeDays,
                dryRun
            )
        }
    }
}
