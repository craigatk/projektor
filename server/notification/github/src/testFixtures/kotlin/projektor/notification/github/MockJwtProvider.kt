package projektor.notification.github

class MockJwtProvider(private val jwtToken: String) : JwtProvider() {
    override fun createJWT(githubAppId: String?, pemKey: String?, ttlMillis: Long): String {
        return jwtToken
    }
}
