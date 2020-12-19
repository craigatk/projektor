package projektor.notification.github.auth

data class JwtTokenConfig(
    val gitHubAppId: String,
    val pemContents: String,
    val ttlMillis: Long
)
