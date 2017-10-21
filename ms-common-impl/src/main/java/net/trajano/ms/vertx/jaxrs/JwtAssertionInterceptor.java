package net.trajano.ms.vertx.jaxrs;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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

    public static final String X_JWKS_URI = "X-JWKS-URI";

    public static final String X_JWT_ASSERTION = "X-JWT-Assertion";

    public static final String X_JWT_AUDIENCE = "X-JWT-Audience";

    private JwtAssertionRequiredPredicate assertionRequiredPredicate;

    @Autowired(required = false)
    @Qualifier("authz.audience")
    private URI audience;

    private JwtClaimsProcessor claimsProcessor;

    @Autowired(required = false)
    @Qualifier("authz.issuer")
    private URI issuer;

    /**
     * JWKS Map
     */
    private final ConcurrentMap<String, HttpsJwks> jwks = new ConcurrentHashMap<>();

    private JwksProvider jwksProvider;

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(final ContainerRequestContext requestContext) {

        if (!assertionRequiredPredicate.test(resourceInfo)) {
            return;
        }
        final String assertion = requestContext.getHeaderString(X_JWT_ASSERTION);
        if (assertion == null) {
            LOG.warn("Missing assertion on request for {}", requestContext.getUriInfo());
            requestContext.abortWith(Response.status(Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, X_JWT_ASSERTION)
                .entity("missing assertion")
                .build());
            return;
        }
        LOG.debug("assertion={}", assertion);

        final JwtClaims claims;
        try {
            HttpsJwks httpsJwks;
            final String jwksUri = requestContext.getHeaderString(X_JWKS_URI);
            if (jwksUri == null) {
                httpsJwks = null;
            } else {
                httpsJwks = jwks.get(jwksUri);
                if (httpsJwks == null) {
                    httpsJwks = new HttpsJwks(jwksUri);
                }
            }
            final String audience = requestContext.getHeaderString(X_JWT_AUDIENCE);
            claims = jwksProvider.buildConsumer(httpsJwks, audience).processToClaims(assertion);
        } catch (final InvalidJwtException e) {
            LOG.error("JWT invalid", e);
            requestContext.abortWith(Response.status(Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, X_JWT_ASSERTION)
                .entity("JWT invalid")
                .build());
            return;
        }

        requestContext.setSecurityContext(new JwtSecurityContext(claims, requestContext.getUriInfo()));
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

    @PostConstruct
    public void init() {

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
