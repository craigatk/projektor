package projektor.versioncontrol

import io.ktor.config.*
import io.ktor.util.*

@KtorExperimentalAPI
data class VersionControlConfig(
    val gitHubBaseUrl: String?
) {
    companion object {
        fun createVersionControlConfig(applicationConfig: ApplicationConfig): VersionControlConfig {
            val gitHubBaseUrl = applicationConfig.propertyOrNull("ktor.versionControl.gitHubBaseUrl")?.getString()

            return VersionControlConfig(
                gitHubBaseUrl = gitHubBaseUrl
            )
        }
    }
}
