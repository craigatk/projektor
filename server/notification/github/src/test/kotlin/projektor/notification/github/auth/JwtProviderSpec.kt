package projektor.notification.github.auth

import com.auth0.jwt.JWT
import io.kotest.core.spec.style.StringSpec
import strikt.api.expectThat
import strikt.assertions.isNotNull

class JwtProviderSpec : StringSpec() {
    init {
        "should create JWT from private key" {
            val jwtTokenConfig = JwtTokenConfig(
                gitHubAppId = "12345",
                pemContents = loadTextFromFile("fake_private_key.txt"),
                ttlMillis = 60_000
            )

            val jwtProvider = JwtProvider(jwtTokenConfig)

            val token = jwtProvider.createJWT()

            val decodedJwt = JWT().decodeJwt(token)
            expectThat(decodedJwt).isNotNull()
        }
    }

    private fun loadTextFromFile(filename: String) = javaClass
        .getResourceAsStream("/$filename")
        .bufferedReader()
        .readText()
}
