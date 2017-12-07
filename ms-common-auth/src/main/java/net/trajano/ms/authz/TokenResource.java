package net.trajano.ms.authz;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import net.trajano.ms.auth.spi.ClientValidator;
import net.trajano.ms.auth.token.GrantTypes;
import net.trajano.ms.auth.token.IdTokenResponse;
import net.trajano.ms.auth.token.OAuthTokenResponse;
import net.trajano.ms.auth.util.HttpAuthorizationHeaders;
import net.trajano.ms.authz.internal.TokenCache;
import net.trajano.ms.authz.spi.InternalClaimsBuilder;
import net.trajano.ms.core.CryptoOps;
import net.trajano.ms.core.ErrorCodes;
import net.trajano.ms.core.ErrorResponses;

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

    private final ConcurrentMap<URI, HttpsJwks> jwksMap = new ConcurrentHashMap<>();

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
        @FormParam("aud") final String audience,
        @FormParam("refresh_token") final String refreshToken,
        @FormParam("jwks_uri") final URI jwksUri,
        @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

        final String[] clientCredentials = HttpAuthorizationHeaders.parseBasicAuthorization(authorization);

        final String clientId = clientCredentials[0];
        if (!clientValidator.isValid(grantType, clientId, clientCredentials[1])) {
            throw ErrorResponses.unauthorized(ErrorCodes.UNAUTHORIZED_CLIENT, "Unauthorized client", String.format("Basic realm=\"%s\", encoding=\"UTF-8\"", realmName));
        }

        if (GrantTypes.REFRESH_TOKEN.equals(grantType)) {
            return handleRefreshGrant(refreshToken, clientId);
        } else if (GrantTypes.AUTHORIZATION_CODE.equals(grantType)) {
            return handleAuthorizationCodeGrant(code);
        } else if (GrantTypes.JWT_ASSERTION.equals(grantType)) {
            return handleJwtAssertionGrant(assertion, clientId, audience);

        } else {
            throw ErrorResponses.badRequest(ErrorCodes.UNSUPPORT_GRANT_TYPE, "Invalid grant type");
        }

    }

    private IdTokenResponse handleAuthorizationCodeGrant(final String accessToken) {

        if (accessToken == null) {
            throw ErrorResponses.invalidRequest("Missing access token");
        }
        final IdTokenResponse idTokenResponse = tokenCache.get(accessToken);
        if (idTokenResponse == null) {
            throw ErrorResponses.unauthorized(ErrorCodes.UNAUTHORIZED_CLIENT, "Access token was not valid", "Bearer");
        }
        return idTokenResponse;

    }

    /**
     * Takes an assertion and converts it using an {@link InternalClaimsBuilder} to
     * a JWT used internally
     *
     * @param assertion
     *            an external JWT assertion
     * @param clientId
     *            client ID
     * @return OAuth response
     */
    private OAuthTokenResponse handleJwtAssertionGrant(final String assertion,
        final String clientId,
        final String audience) {

        if (assertion == null) {
            throw ErrorResponses.badRequest(ErrorCodes.INVALID_REQUEST, "Missing assertion");
        }
        if (clientId == null) {
            throw ErrorResponses.badRequest(ErrorCodes.INVALID_REQUEST, "Missing client_id");
        }

        try {
            final URI jwksUri = clientValidator.getJwksUri(clientId);
            LOG.debug("jwksUri={}", jwksUri);
            HttpsJwks httpsJwks = null;
            if (jwksUri != null) {
                httpsJwks = jwksMap.computeIfAbsent(jwksUri, uri -> new HttpsJwks(uri.toASCIIString()));
            }

            final JwtConsumerBuilder builder = new JwtConsumerBuilder();

            if (httpsJwks == null) {
                builder.setDisableRequireSignature()
                    .setSkipSignatureVerification();
            } else {
                builder.setVerificationKeyResolver(new HttpsJwksVerificationKeyResolver(httpsJwks));
            }
            if (audience == null) {
                builder.setExpectedAudience(clientId);
            } else {
                builder.setExpectedAudience(clientId, audience);
            }
            final JwtConsumer jwtConsumer = builder
                .build();

            final JwtClaims internalClaims = internalClaimsBuilder.buildInternalJWTClaimsSet(jwtConsumer.processToClaims(assertion));

            if (internalClaims.getSubject() == null) {
                LOG.error("Subject is missing from {}", internalClaims);
                throw ErrorResponses.internalServerError("Subject is missing from the resulting claims set.");
            }

            internalClaims.setGeneratedJwtId();
            internalClaims.setIssuer(issuer.toASCIIString());
            if (audience == null) {
                internalClaims.setAudience(clientId);
            } else {
                internalClaims.setAudience(clientId, audience);
            }
            internalClaims.setIssuedAtToNow();

            final Instant expirationTime = Instant.now().plus(jwtMaximumLifetimeInSeconds, ChronoUnit.SECONDS);
            internalClaims.setExpirationTime(NumericDate.fromMilliseconds(expirationTime.toEpochMilli()));

            return tokenCache.store(cryptoOps.sign(internalClaims), internalClaims.getAudience(), expirationTime);

        } catch (final MalformedClaimException
            | InvalidJwtException e) {
            LOG.error("Unable to parse assertion", e);
            throw ErrorResponses.badRequest(ErrorCodes.INVALID_REQUEST, "Unable to parse assertion");
        }
    }

    private OAuthTokenResponse handleRefreshGrant(final String refreshToken,
        final String clientId) {

        if (refreshToken == null) {
            throw ErrorResponses.badRequest(ErrorCodes.INVALID_REQUEST, "Missing refresh token");
        }
        return tokenCache.refresh(refreshToken, clientId);

    }

}
