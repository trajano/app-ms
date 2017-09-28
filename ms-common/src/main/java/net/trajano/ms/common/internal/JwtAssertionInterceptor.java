package net.trajano.ms.common.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
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

import net.trajano.ms.common.DefaultAssertionRequiredFunction;
import net.trajano.ms.common.JwksProvider;
import net.trajano.ms.common.JwtAssertionRequiredFunction;
import net.trajano.ms.common.JwtClaimsProcessor;

/**
 * This performs assertion check on the header data. It ignores the /jwks and
 * /swagger URLs which should be publically accessible.
 *
 * @author Archimedes Trajano
 */
@Component
@Provider
public class JwtAssertionInterceptor implements
    ContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(JwtAssertionInterceptor.class);

    @Autowired(required = false)
    private JwtAssertionRequiredFunction assertionRequiredFunction;

    @Autowired(required = false)
    @Qualifier("authz.audience")
    private URI audience;

    @Autowired(required = false)
    private JwtClaimsProcessor claimsProcessor;

    @Autowired(required = false)
    @Qualifier("authz.issuer")
    private URI issuer;

    @Autowired
    private JwksProvider jwksProvider;

    private Cache<String, RSAKey> keyCache;

    /**
     * Maximum number of keys to cache.
     */
    private final long MAX_NUMBER_OF_KEYS = 20;

    @Autowired(required = false)
    @Qualifier("authz.signature.jwks.uri")
    private URI signatureJwksUri;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        if (!assertionRequiredFunction.apply(requestContext)) {
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

                if (signatureJwksUri != null) {
                    final JWSVerifier verifier = new RSASSAVerifier(getSigningKey(jws.getHeader().getKeyID()));
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
        if (audience != null) {

            if (!claims.getAudience().contains(audience.toASCIIString())) {
                LOG.warn("Audience {} did not match {} for {}", claims.getAudience(), audience, requestContext.getUriInfo());
                requestContext.abortWith(Response.status(Status.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "JWT")
                    .entity("audience vertification failed")
                    .build());
                return;
            }
        }
        if (issuer != null) {
            if (!claims.getIssuer().equals(issuer.toASCIIString())) {
                LOG.warn("Issuer {} did not match {} for {}", claims.getIssuer(), issuer, requestContext.getUriInfo());
                requestContext.abortWith(Response.status(Status.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "JWT")
                    .entity("issuer vertification failed")
                    .build());
                return;
            }
        }

        if (claims.getExpirationTime() != null && claims.getExpirationTime().after(requestContext.getDate())) {
            LOG.warn("Claims expired for {}", requestContext.getUriInfo());
            requestContext.abortWith(Response.status(Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "JWT")
                .entity("claims expired")
                .build());
            return;
        }

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
    private RSAKey getSigningKey(final String keyId) throws ExecutionException {

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
    public void init() throws MalformedURLException,
        IOException,
        ParseException {

        keyCache = CacheBuilder.newBuilder().maximumSize(MAX_NUMBER_OF_KEYS).build();

        if (signatureJwksUri == null) {
            LOG.warn("authz.signature.jwks.uri not specified, no signature verification will be performed");
        }
        if (claimsProcessor == null) {
            LOG.warn("JwtClaimsProcessor was not defined, will not peform any claims validation");

        }
        if (audience == null) {
            LOG.warn("`authz.audience` was not specified, will accept any audience");
        }
        if (issuer == null) {
            LOG.warn("`authz.issuer` was not specified, will accept any issuer");
        }

        if (assertionRequiredFunction == null) {
            LOG.debug("assertionRequiredFunction  was not specified, will use the default");
            assertionRequiredFunction = new DefaultAssertionRequiredFunction();
        }
    }

    public void setClaimsProcessor(final JwtClaimsProcessor claimsProcessor) {

        this.claimsProcessor = claimsProcessor;
    }

    public void setJwksProvider(final JwksProvider jwksProvider) {

        this.jwksProvider = jwksProvider;
    }

}
