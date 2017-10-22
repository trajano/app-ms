package net.trajano.ms.vertx.jaxrs;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;

import net.trajano.ms.core.JwtClaimsSetPrincipal;
import net.trajano.ms.core.Qualifiers;

public class JwtSecurityContext implements
    SecurityContext {

    private final JwtClaimsSetPrincipal principal;

    /**
     * Roles.
     */
    private final Set<String> roles;

    private final boolean secure;

    public JwtSecurityContext(final JwtClaims claims,
        final UriInfo uriInfo) {

        try {
            principal = new JwtClaimsSetPrincipal(claims);
            secure = "https".equals(uriInfo.getRequestUri().getScheme());

            roles = Collections.unmodifiableSet(claims.getStringListClaimValue(Qualifiers.ROLES).parallelStream().collect(Collectors.toSet()));
        } catch (final MalformedClaimException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public String getAuthenticationScheme() {

        return "X-JWT-Assertion";
    }

    @Override
    public Principal getUserPrincipal() {

        return principal;
    }

    @Override
    public boolean isSecure() {

        return secure;
    }

    @Override
    public boolean isUserInRole(final String role) {

        return roles.contains(role);
    }

}
