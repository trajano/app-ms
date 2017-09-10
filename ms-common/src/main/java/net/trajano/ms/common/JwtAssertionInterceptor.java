package net.trajano.ms.common;

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

    @Autowired
    @Qualifier("authz.audience")
    private URI audience;

    @Autowired
    private JwtClaimsProcessor claimsProcessor;

    @Autowired
    private JwksProvider jwksProvider;

    private HttpsJwks signatureJwks;

    @Autowired
    @Qualifier("authz.signature.jwks.uri")
    private URI signatureJwksUri;

    @PostConstruct
    private void init() {

        LOG.debug("signatureJwksUri={0}", signatureJwksUri);
        signatureJwks = new HttpsJwks(signatureJwksUri.toASCIIString());
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
        final JwtConsumer jwtConsumer = new JwtConsumerBuilder()
            .setDecryptionKeyResolver(jwksProvider.getDecryptionKeyResolver())
            .setVerificationKeyResolver(new HttpsJwksVerificationKeyResolver(signatureJwks)).build();
        final JwtClaims claims = jwtConsumer.processToClaims(assertion);

        final boolean validateClaims = claimsProcessor.validateClaims(claims);
        LOG.debug("{1}.validateClaims result={0}", validateClaims, claimsProcessor);
        return validateClaims;
    }

}
