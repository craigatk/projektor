package projektor.auth

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
data class AuthConfig(val publishToken: String?) {
    companion object {
        const val PublishToken = "X-PROJEKTOR-TOKEN"

        fun createAuthConfig(applicationConfig: ApplicationConfig) = AuthConfig(
            applicationConfig.propertyOrNull("ktor.auth.publishToken")?.getString()
        )
    }
}
