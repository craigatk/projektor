package projektor.notification.github;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.StringReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;

public class JwtProvider {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    String createJWT(String githubAppId, String pemKey, long ttlMillis) throws Exception {
        RSAPrivateKey privateKey = getPemPrivateKey(pemKey);
        Algorithm algorithm = Algorithm.RSA256(null, privateKey);
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        long expMillis = nowMillis + ttlMillis;
        Date exp = new Date(expMillis);

        String token = JWT.create()
                .withIssuer(githubAppId)
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .sign(algorithm);

        return token;
    }

    public static RSAPrivateKey getPemPrivateKey(String mKey) throws Exception {
        PEMParser pemParser = new PEMParser(new StringReader(mKey));
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        Object object = pemParser.readObject();
        KeyPair kp = converter.getKeyPair((PEMKeyPair) object);
        PrivateKey privateKey = kp.getPrivate();
        return (RSAPrivateKey)privateKey;
    }
}
