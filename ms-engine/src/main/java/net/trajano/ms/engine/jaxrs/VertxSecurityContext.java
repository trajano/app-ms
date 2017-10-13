package net.trajano.ms.engine.jaxrs;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import io.vertx.core.http.HttpServerRequest;

public class VertxSecurityContext implements
    SecurityContext {

    private final boolean secure;

    public VertxSecurityContext(final HttpServerRequest event) {

        secure = event.isSSL();
    }

    @Override
    public String getAuthenticationScheme() {

        return null;
    }

    @Override
    public Principal getUserPrincipal() {

        return null;
    }

    @Override
    public boolean isSecure() {

        return secure;
    }

    @Override
    public boolean isUserInRole(final String paramString) {

        return false;
    }
}
