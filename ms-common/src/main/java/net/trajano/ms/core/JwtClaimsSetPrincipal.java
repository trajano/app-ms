package net.trajano.ms.core;

import java.net.URI;
import java.security.Principal;

import com.nimbusds.jwt.JWTClaimsSet;

/**
 * This wraps a JWT claims set as a Principal for use with the Security Context.
 *
 * @author Archimedes Trajano
 */
public class JwtClaimsSetPrincipal implements
    Principal {

    private final String authority;

    private final JWTClaimsSet claimsSet;

    public JwtClaimsSetPrincipal(final JWTClaimsSet claimsSet) {

        this.claimsSet = claimsSet;
        authority = String.format("%s@%s", claimsSet.getSubject(), URI.create(claimsSet.getIssuer()).getHost());

    }

    /**
     * @return
     */
    public String getAuthority() {

        return authority;
    }

    public JWTClaimsSet getClaimsSet() {

        return claimsSet;
    }

    @Override
    public String getName() {

        return claimsSet.getSubject();
    }

}
