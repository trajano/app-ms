package net.trajano.ms.vertx.jaxrs;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;

import net.trajano.ms.common.oauth.OAuthTokenResponse;
import net.trajano.ms.vertx.beans.DefaultAssertionRequiredPredicate;
import net.trajano.ms.vertx.beans.JwksProvider;
import net.trajano.ms.vertx.beans.JwtAssertionRequiredPredicate;
import net.trajano.ms.vertx.beans.JwtClaimsProcessor;

/**
 * This performs assertion check on the header data. It ignores the /jwks and
 * /swagger URLs which should be publicly accessible.
 *
 * @author Archimedes Trajano
 */
@Component
@Provider
@Priority(Priorities.AUTHORIZATION)
public class JwtAssertionInterceptor implements
    ContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(JwtAssertionInterceptor.class);

    /**
     * Maximum number of keys to cache.
     */
    private static final long MAX_NUMBER_OF_KEYS = 20;

    private JwtAssertionRequiredPredicate assertionRequiredPredicate;

    @Autowired(required = false)
    @Qualifier("authz.audience")
    private URI audience;

    private JwtClaimsProcessor claimsProcessor;

    @Autowired(required = false)
    @Qualifier("authz.issuer")
    private URI issuer;

    private JwksProvider jwksProvider;

    @Autowired
    private JwksUriProvider jwksUriProvider;

    /**
     * In-memory public key cache.
     */
    private Cache<String, RSAKey> keyCache;

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        if (!assertionRequiredPredicate.test(resourceInfo)) {
            return;
        }
        final String assertion = requestContext.getHeaderString("X-JWT-Assertion");
        if (assertion == null) {
            LOG.warn("Missing assertion on request for {}", requestContext.getUriInfo());
            requestContext.abortWith(Response.status(Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "JWT")
                .entity("missing assertion")
                .build());
            return;
        }
        LOG.debug("assertion={}", assertion);

        final JWTClaimsSet claims;
        try {
            JOSEObject joseObject = JOSEObject.parse(assertion);
            if (joseObject instanceof JWEObject) {
                final JWEObject jwe = (JWEObject) joseObject;
                jwe.decrypt(new RSADecrypter(jwksProvider.getDecryptionKey(
                    jwe.getHeader().getKeyID())));
                joseObject = JOSEObject.parse(jwe.getPayload().toString());
            }

            if (joseObject instanceof JWSObject) {
                final JWSObject jws = (JWSObject) joseObject;

                final URI signatureJwksUri = jwksUriProvider.getUri(requestContext);
                if (signatureJwksUri != null) {
                    final JWSVerifier verifier = new RSASSAVerifier(getSigningKey(jws.getHeader().getKeyID(), signatureJwksUri));
                    if (!jws.verify(verifier)) {
                        LOG.warn("JWT verification failed for {}", requestContext.getUriInfo());
                        requestContext.abortWith(Response.status(Status.UNAUTHORIZED)
                            .header(HttpHeaders.WWW_AUTHENTICATE, "JWT")
                            .entity("signature vertification failed")
                            .build());
                        return;
                    }
                }
            }
            claims = JWTClaimsSet.parse(joseObject.getPayload().toString());
        } catch (final ParseException
            | JOSEException
            | ExecutionException e) {
            throw new BadRequestException("unable to parse JWT");
        }
        if (audience != null && !claims.getAudience().contains(audience.toASCIIString())) {
            LOG.warn("Audience {} did not match {} for {}", claims.getAudience(), audience, requestContext.getUriInfo());
            requestContext.abortWith(OAuthTokenResponse.unauthorized("invalid_audience", "Audience validation failed", "Bearer").getResponse());
            return;

        }
        if (issuer != null && !claims.getIssuer().equals(issuer.toASCIIString())) {
            LOG.warn("Issuer {} did not match {} for {}", claims.getIssuer(), issuer, requestContext.getUriInfo());
            requestContext.abortWith(OAuthTokenResponse.unauthorized("invalid_issuer", "Issuer validation failed", "Bearer").getResponse());
            return;
        }

        if (claims.getExpirationTime() != null && claims.getExpirationTime().before(requestContext.getDate())) {
            LOG.warn("Claims expired for {}", requestContext.getUriInfo());
            requestContext.abortWith(OAuthTokenResponse.unauthorized("invalid_claims", "Claims expired", "Bearer").getResponse());
            return;
        }

        requestContext.setSecurityContext(new JwtSecurityContext(claims.getClaims(), requestContext.getUriInfo()));
        if (claimsProcessor != null) {
            final boolean validateClaims = claimsProcessor.apply(claims);
            LOG.debug("{}.validateClaims result={}", claimsProcessor, validateClaims);
            if (!validateClaims) {
                LOG.warn("Validation of claims failed on request for {}", requestContext.getUriInfo());
                requestContext.abortWith(Response.status(Status.FORBIDDEN)
                    .entity("claims validation failed")
                    .build());
            }
        }
    }

    /**
     * Obtains the key from cache and if not the JWKs.
     *
     * @param keyId
     *            key ID
     * @return RSAKey with public key.
     * @throws ExecutionException
     */
    private RSAKey getSigningKey(final String keyId,
        final URI signatureJwksUri) throws ExecutionException {

        final RSAKey key = keyCache.get(keyId, () -> {

            final JWKSet signatureJwks = JWKSet.load(signatureJwksUri.toURL());
            signatureJwks.getKeys().forEach(t -> keyCache.put(t.getKeyID(), (RSAKey) t));
            return keyCache.getIfPresent(keyId);
        });
        if (key == null) {
            LOG.error("kid={} was not found in the key cache or {}", keyId, signatureJwksUri);
        }
        return key;
    }

    @PostConstruct
    public void init() {

        keyCache = CacheBuilder.newBuilder().maximumSize(MAX_NUMBER_OF_KEYS).build();

        if (audience == null) {
            LOG.warn("`authz.audience` was not specified, will accept any audience");
        }
        if (issuer == null) {
            LOG.warn("`authz.issuer` was not specified, will accept any issuer");
        }
        if (claimsProcessor == null) {
            LOG.warn("JwtClaimsProcessor was not defined, will not peform any claims validation");
        }
        if (assertionRequiredPredicate == null) {
            LOG.debug("assertionRequiredPredicate was not defined, default annotation based predicate will be used");
            assertionRequiredPredicate = new DefaultAssertionRequiredPredicate();
        }

    }

    @Autowired(required = false)
    public void setAssertionRequiredFunction(final JwtAssertionRequiredPredicate predicate) {

        assertionRequiredPredicate = predicate;
    }

    @Autowired(required = false)
    public void setClaimsProcessor(final JwtClaimsProcessor claimsProcessor) {

        this.claimsProcessor = claimsProcessor;
    }

    @Autowired
    public void setJwksProvider(final JwksProvider jwksProvider) {

        this.jwksProvider = jwksProvider;
    }

}
