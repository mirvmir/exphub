package io.github.mirvmir.identity.application;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.github.mirvmir.identity.application.properties.JwtProperties;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Service
public class JwtService {

    private final KeyLoader keyLoader;
    private final JwtProperties properties;

    public String generateToken(Long userId,
                                Collection<? extends GrantedAuthority> authorities,
                                boolean profileCompleted) {
        try {
            Instant now = Instant.now();
            Date issuedAt = Date.from(now);
            Date expiresAt = Date.from(now.plus(properties.getExpiration()));

            List<String> roles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(String.valueOf(userId))
                    .claim("role", roles)
                    .claim("profileCompleted", profileCompleted)
                    .issueTime(issuedAt)
                    .expirationTime(expiresAt)
                    .build();

            RSAPrivateKey privateKey = keyLoader.loadPrivateKey();
            RSAPublicKey publicKey = keyLoader.loadPublicKey();

            RSAKey rsaJwk = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID("kid-1") // не используется, но пускай будет
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .keyID(rsaJwk.getKeyID())
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claims);

            JWSSigner signer = new RSASSASigner(rsaJwk);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("JWT generation failed", e);
        }
    }
}
