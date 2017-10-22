package net.trajano.ms.vertx.beans;

import java.util.function.Function;

import org.jose4j.jwt.JwtClaims;

/**
 * Performs validation on the claims. This is used to perform the
 * authorizations.
 */
public interface JwtClaimsProcessor extends
    Function<JwtClaims, Boolean> {

}
