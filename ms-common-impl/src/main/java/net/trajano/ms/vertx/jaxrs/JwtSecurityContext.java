package net.trajano.ms.vertx.jaxrs;

import java.security.Principal;
import java.util.Set;

import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.jose4j.jwt.JwtClaims;

import net.trajano.ms.core.JwtClaimsSetPrincipal;

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

        principal = new JwtClaimsSetPrincipal(claims);
        secure = "https".equals(uriInfo.getRequestUri().getScheme());

        roles = principal.getRoles();

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
