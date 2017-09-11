package net.trajano.ms.common;

import com.nimbusds.jwt.JWTClaimsSet;

public interface JwtClaimsProcessor {

    /**
     * Performs validation on the claims. This is used to perform the
     * authorizations.
     *
     * @param claims
     * @return <code>true</code> if the claims are valid for the application.
     */
    boolean validateClaims(JWTClaimsSet claims);
}
