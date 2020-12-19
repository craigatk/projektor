package projektor.notification

import io.ktor.config.*
import io.ktor.util.*

@KtorExperimentalAPI
data class NotificationConfig(val serverBaseUrl: String?) {
    companion object {
        fun createNotificationConfig(applicationConfig: ApplicationConfig): NotificationConfig {
            val serverBaseUrl = applicationConfig.propertyOrNull("ktor.notification.serverBaseUrl")?.getString()

            return NotificationConfig(serverBaseUrl)
        }
    }
}
