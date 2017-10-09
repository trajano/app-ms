package net.trajano.ms.common.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.BasicAuthDefinition;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import net.trajano.ms.common.JwtNotRequired;

/**
 * This endpoint routes to specified grant handlers and acts as a OAuth 2.0
 * endpoint.
 *
 * @author Archimedes Trajano
 */
@SwaggerDefinition(
    securityDefinition = @SecurityDefinition(basicAuthDefinitions = @BasicAuthDefinition(key = "client",
        description = "Client ID/Secret")))
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
     * @param authorization
     *            contents of the Authorization header
     */
    private void checkClientAuthorized(final String grantType,
        final String authorization) {

        if (authorization == null || !authorization.startsWith("Basic ")) {
            throw OAuthTokenResponse.unauthorized("invalid_request", "Missing authorization", "Basic");
        }
        final String[] decoded = new String(Base64.getDecoder().decode(authorization.substring(6)), StandardCharsets.US_ASCII).split(":");

        try {
            final String clientId = URLDecoder.decode(decoded[0], "UTF-8");
            final String clientSecret = URLDecoder.decode(decoded[1], "UTF-8");

            if (!clientValidator.isValid(grantType, clientId, clientSecret)) {
                throw OAuthTokenResponse.unauthorized("unauthorized_client", "Client not authorized", "Basic");
            }
        } catch (final UnsupportedEncodingException e) {
            throw OAuthTokenResponse.internalServerError(e);
        }

    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "grant_type",
            value = "Grant type",
            required = true,
            dataType = "java.lang.String",
            paramType = "form")
    })
    @ApiOperation(value = "Token Endpoint",
        authorizations = @Authorization("client"))
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response token(
        @Context final HttpHeaders httpHeaders,
        final MultivaluedMap<String, String> form) {

        final String grantType = form.getFirst("grant_type");

        if (!grantHandlerMap.containsKey(grantType)) {
            throw OAuthTokenResponse.badRequest("unsupported_grant_type", "Unsupported grant type");
        }
        final String basicAuthorizationHeader = httpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION).stream().filter(s -> s.startsWith("Basic ")).collect(Collectors.toList()).get(0);
        checkClientAuthorized(grantType, basicAuthorizationHeader);
        return Response.ok(grantHandlerMap.get(grantType).handler(jaxRsClient, httpHeaders, form)).build();
    }

}
