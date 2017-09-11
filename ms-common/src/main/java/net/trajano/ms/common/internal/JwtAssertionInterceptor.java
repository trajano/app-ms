package net.trajano.ms.common.internal;

import java.net.URI;

import javax.annotation.PostConstruct;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;

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
    @Qualifier("authz.audience")
    private URI audience;

    @Autowired(required = false)
    private JwtClaimsProcessor claimsProcessor;

    @Autowired(required = false)
    @Qualifier("authz.issuer")
    private URI issuer;

    @Autowired
    private JwksProvider jwksProvider;

    private HttpsJwks signatureJwks;

    @Autowired(required = false)
    @Qualifier("authz.signature.jwks.uri")
    private URI signatureJwksUri;

    @PostConstruct
    public void init() {

        if (signatureJwksUri == null) {
            LOG.debug("authz.signature.jwks.uri not specified, no signature verification will be performed");
        } else {
            LOG.debug("signatureJwksUri={}", signatureJwksUri);
            signatureJwks = new HttpsJwks(signatureJwksUri.toASCIIString());
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

        LOG.debug("uri={}", request.getUri());
        if ("/jwks".equals(request.getUri())) {
            return true;
        } else if ("/swagger".equals(request.getUri())) {
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
        final JwtConsumerBuilder builder = new JwtConsumerBuilder()
            .setDecryptionKeyResolver(jwksProvider.getDecryptionKeyResolver());
        if (signatureJwks != null) {
            builder.setVerificationKeyResolver(new HttpsJwksVerificationKeyResolver(signatureJwks));
        } else {
            builder.setSkipSignatureVerification();
        }
        if (audience != null) {
            builder.setExpectedAudience(audience.toASCIIString());
        }
        if (issuer != null) {
            builder.setExpectedIssuer(issuer.toASCIIString());
        }
        final JwtConsumer jwtConsumer = builder.build();
        final JwtClaims claims = jwtConsumer.processToClaims(assertion);

        if (claimsProcessor == null) {
            return true;
        } else {
            final boolean validateClaims = claimsProcessor.validateClaims(claims);
            LOG.debug("{}.validateClaims result={}", claimsProcessor, validateClaims);
            if (!validateClaims) {
                LOG.warn("Missing assertion on request for {}", request.getUri());
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
