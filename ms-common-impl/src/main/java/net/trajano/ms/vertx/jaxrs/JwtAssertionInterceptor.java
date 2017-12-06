package net.trajano.ms.vertx.jaxrs;

import static net.trajano.ms.core.ErrorCodes.FORBIDDEN;
import static net.trajano.ms.core.ErrorCodes.UNAUTHORIZED_CLIENT;
import static net.trajano.ms.core.Qualifiers.REQUEST_ID;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
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

import net.trajano.ms.vertx.beans.CachedDataProvider;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import net.trajano.ms.core.ErrorResponse;
import net.trajano.ms.vertx.beans.DefaultAssertionRequiredPredicate;
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

    private JwtClaimsProcessor claimsProcessor;

    @Autowired(required = false)
    @Qualifier("authz.issuer")
    private URI issuer;

    /**
     * JWKS Map
     */
    private final ConcurrentMap<String, HttpsJwks> jwks = new ConcurrentHashMap<>();

    private CachedDataProvider cachedDataProvider;

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
                .entity(new ErrorResponse(UNAUTHORIZED_CLIENT, "Missing assertion", requestContext.getHeaderString(REQUEST_ID)))
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
            final List<String> audience = Arrays.asList(requestContext.getHeaderString(X_JWT_AUDIENCE).split(", "));
            claims = cachedDataProvider.buildConsumer(httpsJwks, audience).processToClaims(assertion);
        } catch (final InvalidJwtException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("JWT invalid", e);
            } else {
                LOG.error("JWT Invalid");
            }
            requestContext.abortWith(Response.status(Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, X_JWT_ASSERTION)
                .entity(new ErrorResponse(UNAUTHORIZED_CLIENT, "JWT was not valid", requestContext.getHeaderString(REQUEST_ID)))
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
                    .entity(new ErrorResponse(FORBIDDEN, "Claims validation failed", requestContext.getHeaderString(REQUEST_ID)))
                    .build());
            }
        }
    }

    @PostConstruct
    public void init() {

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
    public void setCachedDataProvider(final CachedDataProvider cachedDataProvider) {

        this.cachedDataProvider = cachedDataProvider;
    }

}
