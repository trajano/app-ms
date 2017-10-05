package net.trajano.ms.common;

import java.net.URI;
import java.security.Principal;

import javax.ws.rs.core.UriBuilder;

import com.nimbusds.jwt.JWTClaimsSet;

public class IdTokenPrincipal implements
    Principal {

    private final String authority;

    private final JWTClaimsSet claimsSet;

    public IdTokenPrincipal(final JWTClaimsSet claimsSet) {

        this.claimsSet = claimsSet;
        final URI build = UriBuilder.fromUri(claimsSet.getIssuer()).userInfo(claimsSet.getSubject()).build();
        authority = build.getAuthority();

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
