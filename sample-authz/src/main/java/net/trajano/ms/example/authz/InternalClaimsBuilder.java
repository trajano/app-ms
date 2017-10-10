package net.trajano.ms.example.authz;

import com.nimbusds.jwt.JWTClaimsSet;

public interface InternalClaimsBuilder {

    /**
     * This is an extension point that allows the assembly of an internal JWT
     * Claims set based on data from another JWT Claims Set. It must return a
     * builder rather than a final one as additional framework level claims are
     * required. At minimum the subject needs to be set and optionally the
     * "roles" claim can be populated as well.
     * <p>
     * The implementation can use the claims to look up from another data store
     * to get the internal claims used by the application.
     *
     * @param claims
     *            claims
     * @return JWTClaimsSet.Builder
     */
    JWTClaimsSet.Builder buildInternalJWTClaimsSet(final JWTClaimsSet claims);
}
