package projektor.auth

import io.ktor.server.config.ApplicationConfig

data class AuthConfig(val publishToken: String?) {
    companion object {
        const val PUBLISH_TOKEN = "X-PROJEKTOR-TOKEN"

        fun createAuthConfig(applicationConfig: ApplicationConfig) =
            AuthConfig(
                applicationConfig.propertyOrNull("ktor.auth.publishToken")?.getString(),
            )
    }
}
