package projektor.notification.github.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.StringReader
import java.security.Security
import java.security.interfaces.RSAPrivateKey
import java.util.Date

open class JwtProvider(private val jwtTokenConfig: JwtTokenConfig) {
    init {
        Security.addProvider(BouncyCastleProvider())
    }

    open fun createJWT(): String {
        val privateKey = getPemPrivateKey(jwtTokenConfig.pemContents)
        val algorithm = Algorithm.RSA256(null, privateKey)
        val nowMillis = System.currentTimeMillis()
        val now = Date(nowMillis)

        val expMillis: Long = nowMillis + jwtTokenConfig.ttlMillis
        val exp = Date(expMillis)

        return JWT.create()
            .withIssuer(jwtTokenConfig.gitHubAppId)
            .withIssuedAt(now)
            .withExpiresAt(exp)
            .sign(algorithm)
    }

    companion object {
        private fun getPemPrivateKey(mKey: String): RSAPrivateKey {
            val pemParser = PEMParser(StringReader(mKey))
            val converter = JcaPEMKeyConverter().setProvider("BC")
            val pemKeyPair = pemParser.readObject() as PEMKeyPair
            val kp = converter.getKeyPair(pemKeyPair)
            val privateKey = kp.private
            return privateKey as RSAPrivateKey
        }
    }
}
