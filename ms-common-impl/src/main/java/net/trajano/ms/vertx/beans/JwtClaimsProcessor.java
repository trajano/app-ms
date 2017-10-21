package net.trajano.ms.vertx.beans;

import org.jose4j.jwt.JwtClaims;

import java.util.function.Function;

/**
 * Performs validation on the claims. This is used to perform the
 * authorizations.
 */
public interface JwtClaimsProcessor extends
    Function<JwtClaims, Boolean> {

}
