package net.trajano.ms.core;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwt.JwtClaims;

/**
 * Common cryptography operations.
 */
public interface CryptoOps {

    /**
     * Generates a random string token suitable for non-durable cache lookups.
     * Do not use these in place of UUIDs.
     * 
     * @return string token
     */
    String newToken();

    String sign(JwtClaims claims);

    JwtClaims toClaimsSet(String idToken,
        JsonWebKeySet jwks);

    JwtClaims toClaimsSet(String idToken,
        HttpsJwks jwks);
}
