package net.trajano.ms.common.oauth;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;

public interface GrantHandler {

    String getGrantTypeHandled();

    /**
     * Handles the grant.
     *
     * @param requestContext
     *            request context. Used to get header values if needed
     * @param form
     *            form data that was passed in.
     * @return OAuth token response
     */
    OAuthTokenResponse handler(ContainerRequestContext requestContext,
        MultivaluedMap<String, String> form);
}
