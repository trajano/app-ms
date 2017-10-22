package net.trajano.ms.core;

import java.net.URI;
import java.security.Principal;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;

/**
 * This wraps a JWT claims set as a Principal for use with the Security Context.
 *
 * @author Archimedes Trajano
 */
public class JwtClaimsSetPrincipal implements
    Principal {

    private final String authority;

    private final JwtClaims claimsSet;

    private final String subject;

    /**
     * Build the principal using a map.
     *
     * @param claimsSet
     */
    public JwtClaimsSetPrincipal(final JwtClaims claimsSet) {

        try {
            this.claimsSet = claimsSet;
            subject = claimsSet.getSubject();
            authority = String.format("%s@%s", subject, URI.create(claimsSet.getIssuer()).getHost());

        } catch (final MalformedClaimException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * The authority string consists of the subject '@' issuer.
     *
     * @return an authority string
     */
    public String getAuthority() {

        return authority;
    }

    /**
     * @return the underlying claims set
     */
    public JwtClaims getClaimsSet() {

        return claimsSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {

        return subject;
    }

}
