package net.trajano.ms.authz;

import java.net.URI;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.trajano.ms.auth.util.AuthorizationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import net.trajano.ms.auth.spi.ClientValidator;
import net.trajano.ms.auth.util.HttpAuthorizationHeaders;
import net.trajano.ms.authz.internal.TokenCache;
import net.trajano.ms.authz.internal.TokenCacheEntry;
import net.trajano.ms.core.ErrorResponses;

/**
 * Client check resource.
 *
 * @author Archimedes Trajano
 */
@Api
@Component
@Path("/check")
@PermitAll
public class ClientCheckResource {

    @Autowired
    private ClientValidator clientValidator;

    @Autowired
    private TokenCache tokenCache;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response validateClient(final ClientCheckRequest req) {

        boolean validClient = false;
        final AuthorizationType authorizationType = HttpAuthorizationHeaders.getAuthorizationType(req.getAuthorization());
        if (authorizationType == AuthorizationType.BASIC) {
            validClient = clientValidator.isOriginAllowedFromAuthorization(URI.create(req.getOrigin()), req.getAuthorization());

        } else if (authorizationType == AuthorizationType.BEARER) {
            final String accessToken = HttpAuthorizationHeaders.parseBeaerAuthorization(req.getAuthorization());
            final TokenCacheEntry cacheEntry = tokenCache.getCacheEntry(accessToken);
            if (cacheEntry == null) {
                throw ErrorResponses.invalidRequest("access_token is not valid");
            }
            final String clientId = cacheEntry.getAudiences().iterator().next();
            validClient = clientValidator.isOriginAllowed(clientId, req.getOrigin());

        }

        if (validClient) {
            return Response.noContent().build();
        } else {
            throw ErrorResponses.invalidRequest("Invalid Origin for Client");
        }

    }
}
