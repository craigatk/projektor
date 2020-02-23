package projektor.auth

import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class AuthService(private val authConfig: AuthConfig) {
    fun isAuthValid(tokenFromRequest: String?) =
            authConfig.publishToken.isNullOrEmpty() || authConfig.publishToken == tokenFromRequest
}
