package projektor.notification.github.auth

class MockJwtProvider(private val jwtToken: String) : JwtProvider(JwtTokenConfig("", "", 1)) {
    override fun createJWT(): String {
        return jwtToken
    }
}
