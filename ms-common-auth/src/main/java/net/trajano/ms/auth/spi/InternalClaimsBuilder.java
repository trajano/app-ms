package net.trajano.ms.auth.spi;

import org.jose4j.jwt.JwtClaims;

public interface InternalClaimsBuilder {

    /**
     * This is an extension point that allows the assembly of an internal JWT
     * Claims set based on data from another JWT Claims Set. The claims it would
     * be building should at minimum state the subject and the roles. Also note
     * that the resulting JwtClaims would still be modified to have additional
     * fields that are required by the framework.
     * <p>
     * The implementation can use the claims to look up from another data store
     * to get the internal claims used by the application.
     *
     * @param assertion
     *            external JWT assertion
     * @return JWTClaimsSet.Builder
     */
    JwtClaims buildInternalJWTClaimsSet(final String assertion);

}
