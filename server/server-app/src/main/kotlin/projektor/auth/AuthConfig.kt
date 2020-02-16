package projektor.auth

import io.ktor.config.ApplicationConfig

data class AuthConfig(val publishToken: String?) {
    companion object {
        const val PublishToken = "X-PUBLISH-TOKEN"

        fun createAuthConfig(applicationConfig: ApplicationConfig) = AuthConfig(
                applicationConfig.propertyOrNull("ktor.auth.publishToken")?.getString()
        )
    }
}
