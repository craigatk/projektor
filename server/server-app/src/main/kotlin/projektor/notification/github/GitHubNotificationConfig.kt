package projektor.notification.github

import io.ktor.server.config.ApplicationConfig
import projektor.notification.github.auth.PrivateKeyEncoder

data class GitHubNotificationConfig(
    val gitHubApiUrl: String?,
    val gitHubAppId: String?,
    val privateKey: String?
) {
    companion object {
        fun createGitHubNotificationConfig(applicationConfig: ApplicationConfig): GitHubNotificationConfig {
            val gitHubApiUrl = applicationConfig.propertyOrNull("ktor.notification.gitHub.gitHubApiUrl")?.getString()
            val gitHubAppId = applicationConfig.propertyOrNull("ktor.notification.gitHub.gitHubAppId")?.getString()

            val encodedPrivateKey = applicationConfig.propertyOrNull("ktor.notification.gitHub.privateKey")?.getString()
            val privateKey = encodedPrivateKey?.let { PrivateKeyEncoder.base64Decode(it) }

            return GitHubNotificationConfig(
                gitHubApiUrl = gitHubApiUrl,
                gitHubAppId = gitHubAppId,
                privateKey = privateKey
            )
        }
    }
}
