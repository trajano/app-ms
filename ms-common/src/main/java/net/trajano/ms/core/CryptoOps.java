package net.trajano.ms.core;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwt.JwtClaims;

/**
 * Common cryptography operations.
 */
public interface CryptoOps {

    /**
     * Generates a random string token suitable for non-durable cache lookups. Do
     * not use these in place of UUIDs.
     *
     * @return string token
     */
    String newToken();

    /**
     * Convert {@link JwtClaims} into a JWS.
     *
     * @param claims
     *            claims
     * @return JWS
     */
    String sign(JwtClaims claims);

    /**
     * Get the claims set with a given JWT with no audience checking.
     *
     * @param jwt
     *            JWT
     * @param jwks
     *            remote JWKS
     * @return
     */
    default JwtClaims toClaimsSet(final String jwt,
        final HttpsJwks jwks) {

        return toClaimsSet(jwt, null, jwks);
    }

    /**
     * @deprecated this method is not used.
     * @return null
     */
    @Deprecated
    default JwtClaims toClaimsSet(final String idToken,
        final JsonWebKeySet jwks) {

        return null;
    }

    /**
     * Get the claims set with a given JWT.
     *
     * @param jwt
     *            JWT
     * @param audience
     *            audience. If null specified then this will disable audience
     *            checking.
     * @param jwks
     *            remote JWKS
     * @return
     */
    JwtClaims toClaimsSet(String jwt,
        String audience,
        HttpsJwks jwks);
}
