package net.trajano.ms.core;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;

import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This wraps a JWT claims set as a Principal for use with the Security Context.
 *
 * @author Archimedes Trajano
 */
public class JwtClaimsSetPrincipal implements
    Principal {

    /**
     * This is a framework specific claim which lists the roles in a String[].
     */
    public static final String ROLES = "roles";

    private final String authority;

    private final String subject;

    /**
     * Roles.
     */
    private final Set<String> roles;

    private final JwtClaims claimsSet;

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
            roles = Collections.unmodifiableSet(claimsSet.getStringListClaimValue(ROLES).parallelStream().collect(Collectors.toSet()));
        } catch (MalformedClaimException e) {
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

    public Set<String> getRoles() {

        return roles;
    }
}
