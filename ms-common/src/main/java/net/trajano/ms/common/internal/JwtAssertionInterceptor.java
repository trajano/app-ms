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
 * This performs assertion check on the header data. It ignores the /jwks URL
 * which should be publically accessible.
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

    @Autowired
    private JwksProvider jwksProvider;

    private HttpsJwks signatureJwks;

    @Autowired(required = false)
    @Qualifier("authz.signature.jwks.uri")
    private URI signatureJwksUri;

    @PostConstruct
    private void init() {

        if (signatureJwksUri == null) {
            LOG.debug("authz.signature.jwks.uri not specified, no signature verification will be performed");
        } else {
            LOG.debug("signatureJwksUri={0}", signatureJwksUri);
            signatureJwks = new HttpsJwks(signatureJwksUri.toASCIIString());
        }
        if (claimsProcessor == null) {
            LOG.warn("JwtClaimsProcessor was not defined, will not peform any claims validation");

        }
        if (audience == null) {
            LOG.warn("`authz.audience` was not specified, will accept any audience");
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

        LOG.debug("uri={0}", request.getUri());
        if ("/jwks".equals(request.getUri())) {
            return true;
        }
        final String assertion = request.getHeader("X-JWT-Assertion");
        if (assertion == null) {
            responder.setHeader(javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE, "JWT");
            responder.setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
            return false;
        }
        LOG.debug("assertion={0}", assertion);
        final JwtConsumerBuilder builder = new JwtConsumerBuilder()
            .setDecryptionKeyResolver(jwksProvider.getDecryptionKeyResolver());
        if (signatureJwks != null) {
            builder.setVerificationKeyResolver(new HttpsJwksVerificationKeyResolver(signatureJwks));
        }
        if (audience != null) {
            builder.setExpectedAudience(audience.toASCIIString());
        }
        final JwtConsumer jwtConsumer = builder.build();
        final JwtClaims claims = jwtConsumer.processToClaims(assertion);

        if (claimsProcessor == null) {
            return true;
        } else {
            final boolean validateClaims = claimsProcessor.validateClaims(claims);
            LOG.debug("{1}.validateClaims result={0}", validateClaims, claimsProcessor);
            return validateClaims;
        }
    }

}
