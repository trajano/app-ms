package net.trajano.ms.example.authz;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;

import net.trajano.ms.common.beans.TokenGenerator;
import net.trajano.ms.common.oauth.GrantHandler;
import net.trajano.ms.common.oauth.GrantTypes;
import net.trajano.ms.common.oauth.OAuthTokenResponse;

@Component
@Configuration
public class JwtGrantHandler implements
    GrantHandler {

    private static final Logger LOG = LoggerFactory.getLogger(JwtGrantHandler.class);

    private final Set<String> allowedIssuers = new HashSet<>();

    @Autowired
    private Environment env;

    @Value("${issuer}")
    private URI issuer;

    @Autowired
    private TokenCache tokenCache;

    @Autowired
    private TokenGenerator tokenGenerator;

    /**
     * This is an extension point that allows the assembly of an internal JWT Claims
     * set based on data from another JWT Claims Set. It must return a builder
     * rather than a final one as additional framework level claims are required. At
     * minimum the subject needs to be set and optionally the "roles" claim can be
     * populated as well.
     *
     * @param claims
     *            claims
     * @return JWTClaimsSet.Builder
     */
    private JWTClaimsSet.Builder buildInternalJWTClaimsSet(final JWTClaimsSet claims) {

        // TODO this should be abstract
        return new JWTClaimsSet.Builder()
            .subject("Internal-Subject" + claims.getSubject())
            .claim("roles", Arrays.asList("user"));
    }

    @Override
    public String getGrantTypeHandled() {

        return GrantTypes.JWT_ASSERTION;
    }

    @Override
    public OAuthTokenResponse handler(final Client jaxRsClient,
        final String clientId,
        final HttpHeaders httpHeaders,
        final MultivaluedMap<String, String> form) {

        final String assertion = form.getFirst("assertion");
        if (assertion == null) {
            throw OAuthTokenResponse.badRequest("invalid_request", "Missing Assertion");
        }

        try {
            final JWSObject jwsObject = JWSObject.parse(assertion);
            final JWTClaimsSet claims = JWTClaimsSet.parse(jwsObject.getPayload().toString());
            validateIssuer(claims);
            // TODO cache
            final JWKSet issuerJwks = JWKSet.load(UriBuilder.fromUri(claims.getIssuer()).path("/.well-known/jwks").build().toURL());
            final String keyID = jwsObject.getHeader().getKeyID();
            final RSAKey signingKey = (RSAKey) issuerJwks.getKeyByKeyId(keyID);
            if (!jwsObject.verify(new RSASSAVerifier(signingKey))) {
                throw OAuthTokenResponse.badRequest("access_denied", "Failed signature verification");
            }

            final JWTClaimsSet internalClaims = buildInternalJWTClaimsSet(claims)
                .issuer(issuer.toASCIIString())
                .audience(clientId)
                .jwtID(tokenGenerator.newToken())
                .issueTime(Date.from(Instant.now()))
                .build();
            if (internalClaims.getSubject() == null) {
                LOG.error("Subject is missing from {}", internalClaims);
                throw OAuthTokenResponse.internalServerError("Subject is missing from the resulting claims set.");
            }

            return tokenCache.store(internalClaims);

        } catch (final ParseException e) {
            throw OAuthTokenResponse.badRequest("invalid_request", "Unable to parse assertion");
        } catch (final IllegalArgumentException
            | UriBuilderException
            | JOSEException
            | IOException e) {
            throw new InternalServerErrorException(e);
        } finally {

        }
    }

    /**
     * Loads the allowed issuers from the environment.
     */
    @PostConstruct
    public void init() {

        int i = 0;
        while (env.containsProperty(String.format("allowedIssuers[%d]", i))) {
            allowedIssuers.add(env.getProperty(String.format("allowedIssuers[%d]", i++)));
        }
    }

    private void validateIssuer(final JWTClaimsSet claims) {

        final String issuer = claims.getIssuer();
        if (!allowedIssuers.contains(issuer)) {
            throw OAuthTokenResponse.badRequest("access_denied", "Issuer is not valid");
        }
    }

}
