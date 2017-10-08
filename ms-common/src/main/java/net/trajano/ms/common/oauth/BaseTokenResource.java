package net.trajano.ms.common.oauth;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import net.trajano.ms.common.JwtNotRequired;

/**
 * This endpoint routes to specified grant handlers and acts as a OAuth 2.0
 * endpoint.
 *
 * @author Archimedes Trajano
 */
@JwtNotRequired
public abstract class BaseTokenResource {

    private final ClientValidator clientValidator;

    private final Map<String, GrantHandler> grantHandlerMap;

    public BaseTokenResource(final ClientValidator clientValidator,
        final List<GrantHandler> grantHandlers) {

        this.clientValidator = clientValidator;
        grantHandlerMap = grantHandlers.stream().collect(Collectors.toConcurrentMap(h -> h.getGrantTypeHandled(), Function.identity()));
    }

    /**
     * This performs a check whether the given client is authorized. It will
     * throw a {@link BadRequestException} with unauthorized_client if it fails.
     *
     * @param grantType
     * @param clientId
     * @param clientSecret
     */
    private void checkClientAuthorized(final String grantType,
        final String clientId,
        final String clientSecret) {

        if (!clientValidator.isValid(grantType, clientId, clientSecret)) {
            final OAuthTokenResponse r = new OAuthTokenResponse();
            r.setError("unauthorized_client");
            throw new BadRequestException(Response.ok(r).build());
        }

    }

    protected BadRequestException invalidGrant() {

        final OAuthTokenResponse r = new OAuthTokenResponse();
        r.setError("invalid_grant");
        return new BadRequestException(Response.ok(r).build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response token(@FormParam("grant_type") final String grantType,
        @FormParam("client_id") final String clientId,
        @FormParam("client_secret") final String clientSecret,
        @Context final ContainerRequestContext requestContext,
        final MultivaluedMap<String, String> form) {

        if (!grantHandlerMap.containsKey(grantType)) {
            throw unsupportedGrantType();
        }
        checkClientAuthorized(grantType, clientId, clientSecret);
        return Response.ok(grantHandlerMap.get(grantType).handler(requestContext, form)).build();
    }

    private BadRequestException unsupportedGrantType() {

        final OAuthTokenResponse r = new OAuthTokenResponse();
        r.setError("unsupported_grant_type");
        return new BadRequestException(Response.ok(r).build());
    }

}
