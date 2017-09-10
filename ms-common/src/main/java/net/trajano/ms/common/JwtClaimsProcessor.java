package net.trajano.ms.common;

import org.jose4j.jwt.JwtClaims;

public interface JwtClaimsProcessor {

    /**
     * Performs validation on the claims. This is used to perform the
     * authorizations.
     *
     * @param claims
     * @return <code>true</code> if the claims are valid for the application.
     */
    boolean validateClaims(JwtClaims claims);
}
