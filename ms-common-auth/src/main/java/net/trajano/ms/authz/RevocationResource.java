package net.trajano.ms.authz;

import java.text.ParseException;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import io.swagger.annotations.Api;
import net.trajano.ms.auth.spi.ClientValidator;
import net.trajano.ms.auth.token.OAuthTokenResponse;
import net.trajano.ms.auth.util.HttpAuthorizationHeaders;
import net.trajano.ms.authz.internal.TokenCache;
import net.trajano.ms.core.ErrorCodes;

/**
 * Revocation endpoint resource.
 *
 * @author Archimedes Trajano
 */
@Api
@Configuration
@Component
@Path("/revoke")
@PermitAll
public class RevocationResource {

    private static final Logger LOG = LoggerFactory.getLogger(RevocationResource.class);

    @Autowired
    private ClientValidator clientValidator;

    /**
     * Maximum life of a JWT token. Past that period, it is expected to no longer be
     * used.
     */
    @Value("${token.jwtMaximumLifetime:86400}")
    private int jwtMaximumLifetimeInSeconds;

    @Value("${realmName:client_credentials}")
    private String realmName;

    @Autowired
    private TokenCache tokenCache;

    /**
     * Performs client credential validation then invokes the revocation process
     * from the token cache.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject revoke(
        @FormParam("token") final String token,
        @FormParam("token_type_hint") final String tokenTypeHint,
        @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

        final String clientId;
        try {
            final String[] clientCredentials = HttpAuthorizationHeaders.parseBasicAuthorization(authorization);

            clientId = clientCredentials[0];
            if (!clientValidator.isValid("revocation", clientId, clientCredentials[1])) {
                throw OAuthTokenResponse.unauthorized(ErrorCodes.UNAUTHORIZED_CLIENT, "Unauthorized client", String.format("Basic realm=\"%s\", encoding=\"UTF-8\"", realmName));
            }
        } catch (final ParseException e) {
            throw OAuthTokenResponse.unauthorized(ErrorCodes.UNAUTHORIZED_CLIENT, "Invalid or missing authorization", String.format("Basic realm=\"%s\", encoding=\"UTF-8\"", realmName));
        }

        if (token == null) {
            throw OAuthTokenResponse.badRequest(ErrorCodes.INVALID_REQUEST, "Missing token");
        }
        if (tokenTypeHint != null && !"refresh_token".equals(tokenTypeHint)) {
            throw OAuthTokenResponse.badRequest(ErrorCodes.UNSUPPORTED_TOKEN_TYPE, "Token type is not supported");
        }

        tokenCache.revokeRefreshToken(token, clientId);
        LOG.debug("revoked token={}", token);

        final JsonObject okReturn = new JsonObject();
        okReturn.addProperty("ok", 1);
        return okReturn;

    }

}
