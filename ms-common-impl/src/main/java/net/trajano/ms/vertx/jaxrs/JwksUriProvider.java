package net.trajano.ms.vertx.jaxrs;

import java.net.URI;

import javax.ws.rs.container.ContainerRequestContext;

public interface JwksUriProvider {

    /**
     * Obtains the JWKS URI based on the request context
     *
     * @param requestContext
     *            request context
     * @return URI
     */
    URI getUri(ContainerRequestContext requestContext);

}
