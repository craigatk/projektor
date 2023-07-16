package projektor.notification

import io.ktor.server.config.ApplicationConfig

data class NotificationConfig(val serverBaseUrl: String?) {
    companion object {
        fun createNotificationConfig(applicationConfig: ApplicationConfig): NotificationConfig {
            val serverBaseUrl = applicationConfig.propertyOrNull("ktor.notification.serverBaseUrl")?.getString()

            return NotificationConfig(serverBaseUrl)
        }
    }
}
