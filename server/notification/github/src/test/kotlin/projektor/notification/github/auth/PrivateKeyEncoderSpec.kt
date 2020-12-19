package projektor.notification.github.auth

import io.kotest.core.spec.style.StringSpec
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class PrivateKeyEncoderSpec : StringSpec() {
    init {
        "should encode and decode back to the same value" {
            val keyContents = """
                -----BEGIN RSA PRIVATE KEY-----
                12345
                67890
                -----END RSA PRIVATE KEY-----
            """.trimIndent()

            val encodedKey = PrivateKeyEncoder.base64Encode(keyContents)
            val decodedKey = PrivateKeyEncoder.base64Decode(encodedKey)

            expectThat(decodedKey).isEqualTo(keyContents)
        }
    }
}
