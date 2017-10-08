package net.trajano.ms.example.authz;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;

import net.trajano.ms.common.oauth.GrantHandler;
import net.trajano.ms.common.oauth.GrantTypes;
import net.trajano.ms.common.oauth.OAuthException;
import net.trajano.ms.common.oauth.OAuthTokenResponse;

@Component
@Configuration
public class JwtGrantHandler implements
    GrantHandler {

    @Autowired
    private AllowedIssuers allowedIssuers;

    @Value("${issuer}")
    private URI issuer;

    @Autowired
    private TokenCache tokenCache;

    private JWTClaimsSet buildInternalJWTClaimsSet(final JWTClaimsSet claims) {

        // TODO this should be abstract
        return new JWTClaimsSet.Builder()
            .subject("Internal-Subject")
            .claim("roles", Arrays.asList("user"))
            .issuer(issuer.toASCIIString())
            .issueTime(Date.from(Instant.now()))
            .build();
    }

    @Override
    public String getGrantTypeHandled() {

        return GrantTypes.JWT_ASSERTION;
    }

    @Override
    public OAuthTokenResponse handler(final Client jaxRsClient,
        final HttpHeaders httpHeaders,
        final MultivaluedMap<String, String> form) {

        final String assertion = form.getFirst("assertion");
        if (assertion == null) {
            throw new OAuthException("invalid_request", "Missing Assertion");
        }

        try {
            final JWSObject jwsObject = JWSObject.parse(assertion);
            final JWTClaimsSet claims = JWTClaimsSet.parse(jwsObject.getPayload().toString());
            validateIssuer(claims);
            // TODO cache
            final JWKSet issuerJwks = JWKSet.load(UriBuilder.fromUri(claims.getIssuer()).path("/.well-known/jwks").build().toURL());
            final RSAKey signingKey = (RSAKey) issuerJwks.getKeyByKeyId(jwsObject.getHeader().getKeyID());
            if (!jwsObject.verify(new RSASSAVerifier(signingKey))) {
                throw new OAuthException("access_denied", "Failed signature verification");
            }

            final JWTClaimsSet internalClaims = buildInternalJWTClaimsSet(claims);

            return tokenCache.store(internalClaims);

        } catch (final ParseException e) {
            throw new OAuthException("invalid_request", "Unable to parse assertion");
        } catch (final IllegalArgumentException
            | UriBuilderException
            | JOSEException
            | IOException e) {
            throw new InternalServerErrorException(e);
        } finally {

        }
    }

    private void validateIssuer(final JWTClaimsSet claims) {

        final String issuer = claims.getIssuer();
        if (!allowedIssuers.isIssuerAllowed(issuer)) {
            throw new OAuthException("access_denied", "Issuer is not valid");
        }
    }

}
