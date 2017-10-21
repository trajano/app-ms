package net.trajano.ms.core;

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
     * Subject claim key.
     */
    public static final String SUBJECT = "sub";

    /**
     * Issuer claim key.
     */
    public static final String ISSUER = "iss";

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

    private final Map<String, Object> claimsSet;

    /**
     * Build the principal using a map.
     * 
     * @param claimsSet
     */
    public JwtClaimsSetPrincipal(final Map<String, Object> claimsSet) {

        this.claimsSet = Collections.unmodifiableMap(claimsSet);
        subject = (String) claimsSet.get(SUBJECT);
        authority = String.format("%s@%s", subject, URI.create((String) claimsSet.get(ISSUER)).getHost());
        final String[] claimRoles = (String[]) claimsSet.getOrDefault(ROLES, new String[0]);
        roles = Collections.unmodifiableSet(Stream.of(claimRoles).collect(Collectors.toSet()));
    }

    /**
     * @return
     */
    public String getAuthority() {

        return authority;
    }

    public Map<String, Object> getClaimsSet() {

        return claimsSet;
    }

    @Override
    public String getName() {

        return subject;
    }

    public Set<String> getRoles() {

        return roles;
    }
}
