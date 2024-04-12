package projektor.cleanup

import io.ktor.server.config.ApplicationConfig

data class CleanupConfig(
    val maxReportAgeDays: Int?,
    val maxAttachmentAgeDays: Int?,
    val dryRun: Boolean,
) {
    val enabled: Boolean
        get() = reportCleanupEnabled || attachmentCleanupEnabled

    val reportCleanupEnabled: Boolean
        get() = maxReportAgeDays != null && maxReportAgeDays > 0

    val attachmentCleanupEnabled: Boolean
        get() = maxAttachmentAgeDays != null && maxAttachmentAgeDays > 0

    companion object {
        fun createCleanupConfig(applicationConfig: ApplicationConfig): CleanupConfig {
            val maxReportAgeDays = applicationConfig.propertyOrNull("ktor.cleanup.maxReportAgeDays")?.getString()?.toInt()
            val maxAttachmentAgeDays = applicationConfig.propertyOrNull("ktor.cleanup.maxAttachmentAgeDays")?.getString()?.toInt()
            val dryRun = applicationConfig.propertyOrNull("ktor.cleanup.dryRun")?.getString()?.toBoolean() ?: false

            return CleanupConfig(
                maxReportAgeDays,
                maxAttachmentAgeDays,
                dryRun,
            )
        }
    }
}
