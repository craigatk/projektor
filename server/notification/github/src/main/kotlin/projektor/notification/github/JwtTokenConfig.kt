package projektor.notification.github

data class JwtTokenConfig(
    val gitHubAppId: String,
    val pemContents: String,
    val ttlMillis: Long
)
