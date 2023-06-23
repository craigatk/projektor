package projektor.notification.github.auth

import java.util.Base64

object PrivateKeyEncoder {
    fun base64Encode(keyContents: String): String =
        Base64.getEncoder().encodeToString(keyContents.toByteArray())

    fun base64Decode(encodedKeyContents: String): String =
        String(Base64.getDecoder().decode(encodedKeyContents))
}
