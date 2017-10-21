package net.trajano.ms.example.authz;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.annotation.security.PermitAll;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilderException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.DefaultJOSEProcessor;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import net.trajano.ms.common.oauth.ClientValidator;
import net.trajano.ms.common.oauth.GrantTypes;
import net.trajano.ms.common.oauth.IdTokenResponse;
import net.trajano.ms.common.oauth.OAuthTokenResponse;
import net.trajano.ms.vertx.beans.TokenGenerator;

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

    @Autowired
    private TokenGenerator tokenGenerator;

    /**
     * Performs client credential validation then dispatches to the appropriate
     * handler for a given grant type.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public OAuthTokenResponse dispatch(
        @ApiParam(allowableValues = "refresh_token, authorization_code") @FormParam("grant_type") @NotNull final String grantType,
        @FormParam("code") final String code,
        @FormParam("assertion") final String assertion,
        @FormParam("refresh_token") final String refreshToken,
        @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

        final String[] clientCredentials = HttpAuthorizationHeaders.parseBasicAuthorization(authorization);

        if (clientCredentials == null) {
            throw OAuthTokenResponse.unauthorized("unauthorized_client", "Missing credentials", String.format("Basic realm=\"%s\", encoding=\"UTF-8\"", realmName));
        }
        final String clientId = clientCredentials[0];
        if (!clientValidator.isValid(grantType, clientId, clientCredentials[1])) {
            throw OAuthTokenResponse.unauthorized("unauthorized_client", "Unauthorized client", String.format("Basic realm=\"%s\", encoding=\"UTF-8\"", realmName));
        }

        if (GrantTypes.REFRESH_TOKEN.equals(grantType)) {
            return handleRefreshGrant(refreshToken, clientId);
        } else if (GrantTypes.AUTHORIZATION_CODE.equals(grantType)) {
            return handleAuthorizationCodeGrant(code, clientId);
        } else if (GrantTypes.JWT_ASSERTION.equals(grantType)) {
            return handleJwtAssertionGrant(assertion, clientId);

        } else {
            throw OAuthTokenResponse.badRequest("invalid_grant", "Invalid grant");
        }

    }

    private IdTokenResponse handleAuthorizationCodeGrant(final String accessToken,
        @NotNull final String clientId) {

        if (accessToken == null) {
            throw OAuthTokenResponse.badRequest("invalid_request", "Missing access token");
        }
        return tokenCache.get(accessToken, clientId);

    }

    private OAuthTokenResponse handleJwtAssertionGrant(final String assertion,
        final String clientId) {

        if (assertion == null) {
            throw OAuthTokenResponse.badRequest("invalid_request", "Missing Assertion");
        }

        try {

            final DefaultJOSEProcessor<SimpleSecurityContext> joseProcessor = new DefaultJOSEProcessor<>();
            joseProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS512,
                new RemoteJWKSet<>(clientValidator.getJwksUri(clientId).toURL())));
            final SimpleSecurityContext securityContext = new SimpleSecurityContext();
            final JWTClaimsSet claims = JWTClaimsSet.parse(joseProcessor.process(assertion, securityContext).toString());

            final JWTClaimsSet internalClaims = internalClaimsBuilder.buildInternalJWTClaimsSet(claims)
                .issuer(issuer.toASCIIString())
                .audience(clientId)
                .jwtID(tokenGenerator.newToken())
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().plus(jwtMaximumLifetimeInSeconds, ChronoUnit.SECONDS)))
                .build();
            if (internalClaims.getSubject() == null) {
                LOG.error("Subject is missing from {}", internalClaims);
                throw OAuthTokenResponse.internalServerError("Subject is missing from the resulting claims set.");
            }

            return tokenCache.store(internalClaims);

        } catch (final BadJOSEException
            | ParseException e) {
            throw OAuthTokenResponse.badRequest("invalid_request", "Unable to parse assertion");
        } catch (final IllegalArgumentException
            | UriBuilderException
            | JOSEException
            | IOException e) {
            throw OAuthTokenResponse.internalServerError(e);

        }
    }

    private OAuthTokenResponse handleRefreshGrant(final String refreshToken,
        @NotNull final String clientId) {

        if (refreshToken == null) {
            throw OAuthTokenResponse.badRequest("invalid_request", "Missing refresh token");
        }
        return tokenCache.refresh(refreshToken, clientId);

    }

}
