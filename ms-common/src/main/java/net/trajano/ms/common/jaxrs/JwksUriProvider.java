package net.trajano.ms.common.jaxrs;

import javax.ws.rs.container.ContainerRequestContext;
import java.net.URI;

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
