package net.trajano.ms.vertx.jaxrs;

import net.trajano.ms.core.JwtClaimsSetPrincipal;

import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.security.Principal;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JwtSecurityContext implements
    SecurityContext {

    private final JwtClaimsSetPrincipal principal;

    /**
     * Roles.
     */
    private final Set<String> roles;

    private final boolean secure;

    public JwtSecurityContext(final Map<String, Object> claimsSet,
        final UriInfo uriInfo) {

        principal = new JwtClaimsSetPrincipal(claimsSet);
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
