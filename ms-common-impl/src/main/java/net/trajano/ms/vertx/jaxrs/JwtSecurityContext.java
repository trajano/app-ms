package net.trajano.ms.vertx.jaxrs;

import java.security.Principal;
import java.text.ParseException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import com.nimbusds.jwt.JWTClaimsSet;

import net.trajano.ms.core.JwtClaimsSetPrincipal;

public class JwtSecurityContext implements
    SecurityContext {

    private final JwtClaimsSetPrincipal principal;

    /**
     * Roles.
     */
    private final Set<String> roles;

    private final boolean secure;

    public JwtSecurityContext(final JWTClaimsSet claimsSet,
        final UriInfo uriInfo) {

        principal = new JwtClaimsSetPrincipal(claimsSet);
        secure = "https".equals(uriInfo.getRequestUri().getScheme());

        try {
            final String[] claimRoles = principal.getClaimsSet().getStringArrayClaim("roles");
            if (claimRoles == null) {
                roles = Collections.emptySet();
            } else {
                roles = Stream.of(claimRoles).collect(Collectors.toSet());
            }
        } catch (final ParseException e) {
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
