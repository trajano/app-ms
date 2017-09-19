package net.trajano.ms.engine;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

public class VertxSecurityContext implements
    SecurityContext {

    @Override
    public String getAuthenticationScheme() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Principal getUserPrincipal() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSecure() {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isUserInRole(final String paramString) {

        // TODO Auto-generated method stub
        return false;
    }
}
