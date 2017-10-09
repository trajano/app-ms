package net.trajano.ms.common.oauth;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
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

    @Context
    private Client jaxRsClient;

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
            throw OAuthTokenResponse.unauthorized("unauthorized_client", "Client not authorized");
        }

    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "grant_type",
            value = "Grant type",
            required = true,
            dataType = "java.lang.String",
            example = GrantTypes.CLIENT_CREDENTIALS,
            paramType = "form"),
        @ApiImplicitParam(name = "client_id",
            value = "Client ID",
            dataType = "java.lang.String",
            required = true,
            paramType = "form"),
        @ApiImplicitParam(name = "client_secret",
            value = "Client Secret",
            dataType = "java.lang.String",
            required = true,
            paramType = "form")
    })
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response token(
        @Context final HttpHeaders httpHeaders,
        final MultivaluedMap<String, String> form) {

        final String grantType = form.getFirst("grant_type");
        final String clientId = form.getFirst("client_id");
        final String clientSecret = form.getFirst("client_secret");

        if (!grantHandlerMap.containsKey(grantType)) {
            throw unsupportedGrantType();
        }
        checkClientAuthorized(grantType, clientId, clientSecret);
        return Response.ok(grantHandlerMap.get(grantType).handler(jaxRsClient, httpHeaders, form)).build();
    }

    private BadRequestException unsupportedGrantType() {

        final OAuthTokenResponse r = new OAuthTokenResponse();
        r.setError("unsupported_grant_type");
        return new BadRequestException(Response.ok(r).build());
    }

}
