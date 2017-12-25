package net.trajano.ms.authz;

import java.net.URI;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import net.trajano.ms.auth.token.GrantTypes;
import net.trajano.ms.auth.util.AuthorizationType;
import net.trajano.ms.auth.util.HttpAuthorizationHeaders;
import net.trajano.ms.authz.internal.TokenCache;
import net.trajano.ms.authz.internal.TokenCacheEntry;
import net.trajano.ms.authz.spi.ClientValidator;
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

    private static final Logger LOG = LoggerFactory.getLogger(ClientCheckResource.class);

    @Autowired
    private ClientValidator clientValidator;

    @Autowired
    private TokenCache tokenCache;

    /**
     * Obtains the redirect URI that can receive the OpenID token as a fragment. It
     * requires that the client supports the openid grant type.
     *
     * @param authorization
     *            authorization header
     * @return redirect URI
     */
    @GET
    @Path("/openid-redirect-uri")
    @Produces(MediaType.TEXT_PLAIN)
    public String redirectUri(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

        LOG.debug("redirect URI for authorization={}", authorization);
        if (!clientValidator.isValid(GrantTypes.OPENID, authorization)) {
            throw ErrorResponses.invalidAuthorization();
        }

        return clientValidator.getRedirectUriFromAuthorization(authorization).toASCIIString();

    }

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
