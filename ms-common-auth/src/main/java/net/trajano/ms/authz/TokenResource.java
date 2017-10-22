package net.trajano.ms.authz;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilderException;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import net.trajano.ms.auth.token.ErrorCodes;
import net.trajano.ms.auth.token.GrantTypes;
import net.trajano.ms.auth.token.IdTokenResponse;
import net.trajano.ms.auth.token.OAuthTokenResponse;
import net.trajano.ms.auth.util.HttpAuthorizationHeaders;
import net.trajano.ms.authz.internal.TokenCache;
import net.trajano.ms.authz.spi.ClientValidator;
import net.trajano.ms.authz.spi.InternalClaimsBuilder;
import net.trajano.ms.core.CryptoOps;

@Api
@Configuration
@Component
@Path("/token")
@PermitAll
public class TokenResource {

    private static final Logger LOG = LoggerFactory.getLogger(TokenResource.class);

    @Autowired
    private ClientValidator clientValidator;

    @Autowired
    private CryptoOps cryptoOps;

    @Autowired
    private InternalClaimsBuilder internalClaimsBuilder;

    @Value("${issuer}")
    private URI issuer;

    /**
     * Maximum life of a JWT token. Past that period, it is expected to no
     * longer be used.
     */
    @Value("${token.jwtMaximumLifetime:86400}")
    private int jwtMaximumLifetimeInSeconds;

    @Value("${realmName:client_credentials}")
    private String realmName;

    @Autowired
    private TokenCache tokenCache;

    /**
     * Performs client credential validation then dispatches to the appropriate
     * handler for a given grant type.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public OAuthTokenResponse dispatch(
        @ApiParam(allowableValues = "refresh_token, authorization_code") @FormParam("grant_type") final String grantType,
        @FormParam("code") final String code,
        @FormParam("assertion") final String assertion,
        @FormParam("refresh_token") final String refreshToken,
        @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

        final String[] clientCredentials = HttpAuthorizationHeaders.parseBasicAuthorization(authorization);

        if (clientCredentials == null) {
            throw OAuthTokenResponse.unauthorized(ErrorCodes.UNAUTHORIZED_CLIENT, "Missing credentials", String.format("Basic realm=\"%s\", encoding=\"UTF-8\"", realmName));
        }
        final String clientId = clientCredentials[0];
        if (!clientValidator.isValid(grantType, clientId, clientCredentials[1])) {
            throw OAuthTokenResponse.unauthorized(ErrorCodes.UNAUTHORIZED_CLIENT, "Unauthorized client", String.format("Basic realm=\"%s\", encoding=\"UTF-8\"", realmName));
        }

        if (GrantTypes.REFRESH_TOKEN.equals(grantType)) {
            return handleRefreshGrant(refreshToken, clientId);
        } else if (GrantTypes.AUTHORIZATION_CODE.equals(grantType)) {
            return handleAuthorizationCodeGrant(code);
        } else if (GrantTypes.JWT_ASSERTION.equals(grantType)) {
            return handleJwtAssertionGrant(assertion, clientId);

        } else {
            throw OAuthTokenResponse.badRequest(ErrorCodes.UNSUPPORT_GRANT_TYPE, "Invalid grant");
        }

    }

    private IdTokenResponse handleAuthorizationCodeGrant(final String accessToken) {

        if (accessToken == null) {
            throw OAuthTokenResponse.badRequest(ErrorCodes.INVALID_REQUEST, "Missing access token");
        }
        final IdTokenResponse idTokenResponse = tokenCache.get(accessToken);
        if (idTokenResponse == null) {
            throw OAuthTokenResponse.unauthorized(ErrorCodes.UNAUTHORIZED_CLIENT, "Access token was not valid", "Bearer");
        }
        return idTokenResponse;

    }

    /**
     * Takes an assertion and converts it using an {@link InternalClaimsBuilder}
     * to a JWT used internally
     *
     * @param assertion
     *            an external JWT assertion
     * @param clientId
     *            client ID
     * @return OAuth response
     */
    private OAuthTokenResponse handleJwtAssertionGrant(final String assertion,
        final String clientId) {

        if (assertion == null) {
            throw OAuthTokenResponse.badRequest(ErrorCodes.INVALID_REQUEST, "Missing assertion");
        }
        if (clientId == null) {
            throw OAuthTokenResponse.badRequest(ErrorCodes.INVALID_REQUEST, "Missing client_id");
        }

        try {

            final JwtClaims internalClaims = internalClaimsBuilder.buildInternalJWTClaimsSet(assertion);

            if (internalClaims.getSubject() == null) {
                LOG.error("Subject is missing from {}", internalClaims);
                throw OAuthTokenResponse.internalServerError("Subject is missing from the resulting claims set.");
            }

            internalClaims.setGeneratedJwtId();
            internalClaims.setIssuer(issuer.toASCIIString());
            internalClaims.setAudience(clientId);
            internalClaims.setIssuedAtToNow();

            final Instant expirationTime = Instant.now().plus(jwtMaximumLifetimeInSeconds, ChronoUnit.SECONDS);
            internalClaims.setExpirationTime(NumericDate.fromMilliseconds(expirationTime.toEpochMilli()));

            return tokenCache.store(cryptoOps.sign(internalClaims), clientId, expirationTime);

        } catch (final MalformedClaimException e) {
            throw OAuthTokenResponse.badRequest(ErrorCodes.INVALID_REQUEST, "Unable to parse assertion");
        } catch (final IllegalArgumentException
            | UriBuilderException e) {
            throw OAuthTokenResponse.internalServerError(e);
        }
    }

    private OAuthTokenResponse handleRefreshGrant(final String refreshToken,
        final String clientId) {

        if (refreshToken == null) {
            throw OAuthTokenResponse.badRequest(ErrorCodes.INVALID_REQUEST, "Missing refresh token");
        }
        return tokenCache.refresh(refreshToken, clientId);

    }

}
