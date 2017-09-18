package net.trajano.ms.common.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
public class JwtAssertionInterceptor implements
    Interceptor {

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

    @Override
    public void postCall(final Request request,
        final int status,
        final ServiceMethodInfo serviceMethodInfo) throws Exception {

    }

    @Override
    public boolean preCall(final Request request,
        final Response responder,
        final ServiceMethodInfo serviceMethodInfo) throws Exception {

        if (!assertionRequiredFunction.apply(request.getUri())) {
            return true;
        }
        final String assertion = request.getHeader("X-JWT-Assertion");
        if (assertion == null) {
            LOG.warn("Missing assertion on request for {}", request.getUri());
            responder.setHeader(javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE, "JWT");
            responder.setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
            responder.setEntity("missing assertion");
            responder.send();
            return false;
        }
        LOG.debug("assertion={}", assertion);

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
                    LOG.warn("JWT verification failed for {}", request.getUri());
                    responder.setHeader(javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE, "JWT");
                    responder.setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
                    responder.setEntity("verification failed");
                    responder.send();
                    return false;
                }
            }
        }
        final JWTClaimsSet claims = JWTClaimsSet.parse(joseObject.getPayload().toString());
        if (audience != null) {

            if (!claims.getAudience().contains(audience.toASCIIString())) {
                LOG.warn("Audience {} did not match {} for {}", claims.getAudience(), audience, request.getUri());
                responder.setHeader(javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE, "JWT");
                responder.setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
                responder.setEntity("audience verification failed");
                responder.send();
                return false;
            }
        }
        if (issuer != null) {
            if (!claims.getIssuer().equals(issuer.toASCIIString())) {
                LOG.warn("Issuer {} did not match {} for {}", claims.getIssuer(), issuer, request.getUri());
                responder.setHeader(javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE, "JWT");
                responder.setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
                responder.setEntity("issuer verification failed");
                responder.send();
                return false;
            }
        }
        if (claims.getExpirationTime() != null && claims.getExpirationTime().after(new Date())) {
            LOG.warn("Claims expired for {}", request.getUri());
            responder.setHeader(javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE, "JWT");
            responder.setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
            responder.setEntity("Claims expired");
            responder.send();
            return false;
        }

        if (claimsProcessor == null) {
            return true;
        } else {
            final boolean validateClaims = claimsProcessor.apply(claims);
            LOG.debug("{}.validateClaims result={}", claimsProcessor, validateClaims);
            if (!validateClaims) {
                LOG.warn("Validation of claims failed on request for {}", request.getUri());
                responder.setHeader(javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE, "JWT");
                responder.setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
                responder.setEntity("claims validation failed");
                responder.send();
                return false;
            } else {
                return true;
            }
        }
    }

    public void setClaimsProcessor(final JwtClaimsProcessor claimsProcessor) {

        this.claimsProcessor = claimsProcessor;
    }

    public void setJwksProvider(final JwksProvider jwksProvider) {

        this.jwksProvider = jwksProvider;
    }

}
