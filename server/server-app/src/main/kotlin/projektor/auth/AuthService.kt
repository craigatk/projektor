package projektor.auth

class AuthService(private val authConfig: AuthConfig) {
    fun isAuthValid(tokenFromRequest: String?) =
            authConfig.publishToken.isNullOrEmpty() || authConfig.publishToken == tokenFromRequest
}
