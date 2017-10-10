package net.trajano.ms.common.oauth;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

public interface GrantHandler {

    String getGrantTypeHandled();

    /**
     * Handles the grant.
     *
     * @param client
     *            JAX-RS Client
     * @param clientId
     *            OAuth Client ID. Used to validate or populate the audience.
     * @param requestContext
     *            request context. Used to get header values if needed
     * @param form
     *            form data that was passed in.
     * @return OAuth token response
     */
    OAuthTokenResponse handler(Client client,
        String clientId,
        HttpHeaders httpHeaders,
        MultivaluedMap<String, String> form);
}
