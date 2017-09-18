package net.trajano.ms.common;

import java.util.function.Function;

import com.nimbusds.jwt.JWTClaimsSet;

/**
 * Performs validation on the claims. This is used to perform the
 * authorizations.
 */
public interface JwtClaimsProcessor extends
    Function<JWTClaimsSet, Boolean> {

}
